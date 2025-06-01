package io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingSpotNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingSpotSaveFailedException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingSpotEntity
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingSpotRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.context.MessageSource

class ParkingSpotRepositoryAdapterTest : BehaviorSpec({
    val messageSource = mockk<MessageSource>(relaxed = true)
    val traceContext = mockk<TraceContext>(relaxed = true)
    val parkingSpotRepository = mockk<ParkingSpotRepository>()

    val parkingSpotRepositoryAdapter =
        ParkingSpotRepositoryAdapter(
            messageSource = messageSource,
            traceContext = traceContext,
            parkingSpotRepository = parkingSpotRepository,
        )

    beforeTest {
        clearAllMocks()

        // Default mocks for all tests
        coEvery { traceContext.traceId() } returns "test-trace-id"
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    given("A parking spot") {
        val parkingId = 1L
        val spotId = 2L
        val latitude = 10.123
        val longitude = 20.456

        val parkingSpotEntity =
            ParkingSpotEntity(
                id = spotId,
                parkingId = parkingId,
                latitude = latitude,
                longitude = longitude,
            )

        `when`("Finding a parking spot by coordinates successfully") {
            beforeTest {
                coEvery {
                    parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
                } returns parkingSpotEntity
            }

            then("Should return the parking spot") {
                val result = parkingSpotRepositoryAdapter.findByCoordinates(latitude, longitude)

                result.id shouldBe spotId
                result.parkingId shouldBe parkingId
                result.latitude shouldBe latitude
                result.longitude shouldBe longitude

                coVerify(exactly = 1) {
                    parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
                }
            }
        }

        `when`("Finding a parking spot by coordinates fails") {
            beforeTest {
                coEvery {
                    parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
                } throws RuntimeException("Database error")
            }

            then("Should throw ParkingSpotNotFoundException") {
                shouldThrow<ParkingSpotNotFoundException> {
                    parkingSpotRepositoryAdapter.findByCoordinates(latitude, longitude)
                }

                coVerify(exactly = 1) {
                    parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
                }
            }
        }

        `when`("Saving a parking spot successfully") {
            val newSpot =
                ParkingSpot(
                    id = null,
                    parkingId = parkingId,
                    latitude = latitude,
                    longitude = longitude,
                )

            beforeTest {
                coEvery {
                    parkingSpotRepository.save(any())
                } returns parkingSpotEntity
            }

            then("Should return the saved parking spot") {
                val result = parkingSpotRepositoryAdapter.save(newSpot)

                result.id shouldBe spotId
                result.parkingId shouldBe parkingId
                result.latitude shouldBe latitude
                result.longitude shouldBe longitude

                coVerify(exactly = 1) {
                    parkingSpotRepository.save(any())
                }
            }
        }

        `when`("Saving a parking spot fails") {
            val newSpot =
                ParkingSpot(
                    id = null,
                    parkingId = parkingId,
                    latitude = latitude,
                    longitude = longitude,
                )

            beforeTest {
                coEvery {
                    parkingSpotRepository.save(any())
                } throws RuntimeException("Database error")
            }

            then("Should throw ParkingSpotSaveFailedException") {
                shouldThrow<ParkingSpotSaveFailedException> {
                    parkingSpotRepositoryAdapter.save(newSpot)
                }

                coVerify(exactly = 1) {
                    parkingSpotRepository.save(any())
                }
            }
        }
    }
})
