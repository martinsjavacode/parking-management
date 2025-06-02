package io.github.martinsjavacode.parkingmanagement.adapters.outbound.redis

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import reactor.core.publisher.Mono
import java.time.Duration

class RedisDistributedLockAdapterTest : BehaviorSpec({
    val reactiveRedisTemplate = mockk<ReactiveRedisTemplate<String, String>>()
    val valueOps = mockk<ReactiveValueOperations<String, String>>()
    val lockAdapter = RedisDistributedLockAdapter(reactiveRedisTemplate)

    Given("A Redis distributed lock adapter") {
        coEvery { reactiveRedisTemplate.opsForValue() } returns valueOps

        When("Acquiring a lock for a parking spot") {
            val latitude = 10.0
            val longitude = 20.0
            val licensePlate = "ABC1234"

            And("The lock is available") {
                coEvery {
                    valueOps.setIfAbsent(any(), eq(licensePlate), any<Duration>())
                } returns Mono.just(true)

                Then("The lock should be acquired") {
                    val result = lockAdapter.acquireLock(latitude, longitude, licensePlate)
                    result shouldBe true
                }
            }

            And("The lock is already taken") {
                coEvery {
                    valueOps.setIfAbsent(any(), eq(licensePlate), any<Duration>())
                } returns Mono.just(false)

                Then("The lock should not be acquired") {
                    val result = lockAdapter.acquireLock(latitude, longitude, licensePlate)
                    result shouldBe false
                }
            }
        }

        When("Releasing a lock") {
            val latitude = 10.0
            val longitude = 20.0
            val licensePlate = "ABC1234"

            And("The lock is owned by the same license plate") {
                coEvery { valueOps.get(any()) } returns Mono.just(licensePlate)
                coEvery { valueOps.delete(any()) } returns Mono.just(true)

                Then("The lock should be released") {
                    val result = lockAdapter.releaseLock(latitude, longitude, licensePlate)
                    result shouldBe true
                }
            }

            And("The lock is owned by a different license plate") {
                coEvery { valueOps.get(any()) } returns Mono.just("XYZ9876")

                Then("The lock should not be released") {
                    val result = lockAdapter.releaseLock(latitude, longitude, licensePlate)
                    result shouldBe false
                }
            }
        }

        When("Checking idempotency") {
            val latitude = 10.0
            val longitude = 20.0
            val eventId = "event-123"

            And("The event has not been processed before") {
                coEvery {
                    valueOps.setIfAbsent(any(), eq(eventId), any<Duration>())
                } returns Mono.just(true)

                Then("It should mark as new and return true") {
                    val result = lockAdapter.checkAndMarkIdempotency(latitude, longitude, eventId)
                    result shouldBe true
                }
            }

            And("The event has been processed before") {
                coEvery {
                    valueOps.setIfAbsent(any(), eq(eventId), any<Duration>())
                } returns Mono.just(false)

                Then("It should return false") {
                    val result = lockAdapter.checkAndMarkIdempotency(latitude, longitude, eventId)
                    result shouldBe false
                }
            }
        }

        When("Releasing an idempotency key") {
            val latitude = 10.0
            val longitude = 20.0

            And("The key exists") {
                coEvery { valueOps.delete(any()) } returns Mono.just(true)

                Then("It should be released successfully") {
                    val result = lockAdapter.releaseIdempotencyKey(latitude, longitude)
                    result shouldBe true
                }
            }

            And("The key doesn't exist") {
                coEvery { valueOps.delete(any()) } returns Mono.just(false)

                Then("It should return false") {
                    val result = lockAdapter.releaseIdempotencyKey(latitude, longitude)
                    result shouldBe false
                }
            }
        }
    }
})
