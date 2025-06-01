package io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.LicensePlateNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingEventNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingEventSaveFailedException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEventEntity
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingEventRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.LocalDateTime

class ParkingEventRepositoryAdapterTest : BehaviorSpec({
    val messageSource = mockk<MessageSource>(relaxed = true)
    val traceContext = mockk<TraceContext>(relaxed = true)
    val parkingEventRepository = mockk<ParkingEventRepository>()

    val parkingEventRepositoryAdapter =
        ParkingEventRepositoryAdapter(
            messageSource = messageSource,
            traceContext = traceContext,
            parkingEventRepository = parkingEventRepository,
        )

    beforeTest {
        clearAllMocks()

        // Default mocks for all tests
        coEvery { traceContext.traceId() } returns "test-trace-id"
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    given("A parking event") {
        val now = LocalDateTime.now()
        val licensePlate = "ABC1234"
        val latitude = 10.123
        val longitude = 20.456

        val parkingEventEntity =
            ParkingEventEntity(
                id = 1L,
                licensePlate = licensePlate,
                latitude = latitude,
                longitude = longitude,
                entryTime = now,
                exitTime = null,
                eventType = EventType.PARKED,
                priceMultiplier = 1.1,
                amountPaid = BigDecimal.ZERO,
            )

        val parkingEvent =
            ParkingEvent(
                id = 1L,
                licensePlate = licensePlate,
                latitude = latitude,
                longitude = longitude,
                entryTime = now,
                exitTime = null,
                eventType = EventType.PARKED,
                priceMultiplier = 1.1,
                amountPaid = BigDecimal.ZERO,
            )

        `when`("Saving a parking event successfully") {
            beforeTest {
                coEvery { parkingEventRepository.save(any()) } returns parkingEventEntity
            }

            then("Should save the parking event without throwing exceptions") {
                parkingEventRepositoryAdapter.save(parkingEvent)

                coVerify(exactly = 1) { parkingEventRepository.save(any()) }
            }
        }

        `when`("Saving a parking event fails") {
            beforeTest {
                coEvery { parkingEventRepository.save(any()) } throws RuntimeException("Database error")
            }

            then("Should throw ParkingEventSaveFailedException") {
                shouldThrow<ParkingEventSaveFailedException> {
                    parkingEventRepositoryAdapter.save(parkingEvent)
                }

                coVerify(exactly = 1) { parkingEventRepository.save(any()) }
            }
        }

        `when`("Finding all events by license plate successfully") {
            beforeTest {
                coEvery { parkingEventRepository.findByLicensePlate(licensePlate) } returns flowOf(parkingEventEntity)
            }

            then("Should return a flow of parking events") {
                val result = parkingEventRepositoryAdapter.findAllByLicensePlate(licensePlate).toList()

                result.size shouldBe 1
                result[0].id shouldBe 1L
                result[0].licensePlate shouldBe licensePlate
                result[0].eventType shouldBe EventType.PARKED

                coVerify(exactly = 1) { parkingEventRepository.findByLicensePlate(licensePlate) }
            }
        }

        `when`("Finding all events by license plate returns null") {
            beforeTest {
                coEvery { parkingEventRepository.findByLicensePlate(licensePlate) } returns null
            }

            then("Should throw LicensePlateNotFoundException") {
                shouldThrow<LicensePlateNotFoundException> {
                    parkingEventRepositoryAdapter.findAllByLicensePlate(licensePlate)
                }

                coVerify(exactly = 1) { parkingEventRepository.findByLicensePlate(licensePlate) }
            }
        }

        `when`("Finding last event by license plate and event type successfully") {
            beforeTest {
                coEvery {
                    parkingEventRepository.findLastByLicensePlateAndEventType(licensePlate, EventType.PARKED)
                } returns parkingEventEntity
            }

            then("Should return the parking event") {
                val result =
                    parkingEventRepositoryAdapter.findLastParkingEventByLicenseAndEventType(
                        licensePlate,
                        EventType.PARKED,
                    )

                result.id shouldBe 1L
                result.licensePlate shouldBe licensePlate
                result.eventType shouldBe EventType.PARKED

                coVerify(exactly = 1) {
                    parkingEventRepository.findLastByLicensePlateAndEventType(licensePlate, EventType.PARKED)
                }
            }
        }

        `when`("Finding last event by license plate and event type throws an exception") {
            beforeTest {
                coEvery {
                    parkingEventRepository.findLastByLicensePlateAndEventType(licensePlate, EventType.PARKED)
                } throws RuntimeException("Database error")
            }

            then("Should throw ParkingEventNotFoundException") {
                shouldThrow<ParkingEventNotFoundException> {
                    parkingEventRepositoryAdapter.findLastParkingEventByLicenseAndEventType(
                        licensePlate,
                        EventType.PARKED,
                    )
                }

                coVerify(exactly = 1) {
                    parkingEventRepository.findLastByLicensePlateAndEventType(licensePlate, EventType.PARKED)
                }
            }
        }

        `when`("Finding most recent event by coordinates successfully") {
            beforeTest {
                coEvery {
                    parkingEventRepository.findLastByLatitudeAndLongitude(latitude, longitude)
                } returns parkingEventEntity
            }

            then("Should return the parking event") {
                val result = parkingEventRepositoryAdapter.findMostRecentByCoordinates(latitude, longitude)

                result.id shouldBe 1L
                result.latitude shouldBe latitude
                result.longitude shouldBe longitude

                coVerify(exactly = 1) {
                    parkingEventRepository.findLastByLatitudeAndLongitude(latitude, longitude)
                }
            }
        }

        `when`("Finding most recent event by coordinates throws an exception") {
            beforeTest {
                coEvery {
                    parkingEventRepository.findLastByLatitudeAndLongitude(latitude, longitude)
                } throws RuntimeException("Database error")
            }

            then("Should throw ParkingEventNotFoundException") {
                shouldThrow<ParkingEventNotFoundException> {
                    parkingEventRepositoryAdapter.findMostRecentByCoordinates(latitude, longitude)
                }

                coVerify(exactly = 1) {
                    parkingEventRepository.findLastByLatitudeAndLongitude(latitude, longitude)
                }
            }
        }
    }
})
