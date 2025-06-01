package io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingCapacityAndOccupancy
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEntity
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingCustomQueryRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.LocalTime

class ParkingCustomQueryRepositoryAdapterTest : BehaviorSpec({
    val messageSource = mockk<MessageSource>(relaxed = true)
    val traceContext = mockk<TraceContext>(relaxed = true)
    val parkingCustomQueryRepository = mockk<ParkingCustomQueryRepository>()

    val parkingCustomQueryRepositoryAdapter =
        ParkingCustomQueryRepositoryAdapter(
            messageSource = messageSource,
            traceContext = traceContext,
            parkingCustomQueryRepository = parkingCustomQueryRepository,
        )

    beforeTest {
        clearAllMocks()

        // Default mocks for all tests
        coEvery { traceContext.traceId() } returns "test-trace-id"
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    given("Coordinates for a parking spot") {
        val latitude = 10.123
        val longitude = 20.456

        val parkingEntity =
            ParkingEntity(
                id = 1L,
                sector = "A",
                basePrice = BigDecimal("10.00"),
                maxCapacity = 100,
                openHour = LocalTime.of(8, 0),
                closeHour = LocalTime.of(20, 0),
                durationLimitMinutes = 60,
            )

        val expectedParking =
            Parking(
                id = 1L,
                sector = "A",
                basePrice = BigDecimal("10.00"),
                maxCapacity = 100,
                openHour = LocalTime.of(8, 0),
                closeHour = LocalTime.of(20, 0),
                durationLimitMinutes = 60,
                spots = flowOf(),
            )

        val parkingCapacityAndOccupancy =
            ParkingCapacityAndOccupancy(
                maxCapacity = 100,
                spotOccupancy = 25,
            )

        `when`("Finding parking capacity and occupancy successfully") {
            beforeTest {
                coEvery {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(latitude, longitude)
                } returns parkingCapacityAndOccupancy
            }

            then("Should return the parking capacity and occupancy") {
                val result = parkingCustomQueryRepositoryAdapter.findParkingCapacityAndOccupancy(latitude, longitude)

                result.maxCapacity shouldBe 100
                result.spotOccupancy shouldBe 25

                coVerify(exactly = 1) {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(latitude, longitude)
                }
            }
        }

        `when`("Finding parking capacity and occupancy returns null") {
            beforeTest {
                coEvery {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(latitude, longitude)
                } returns null
            }

            then("Should return default values") {
                val result = parkingCustomQueryRepositoryAdapter.findParkingCapacityAndOccupancy(latitude, longitude)

                result.maxCapacity shouldBe 1
                result.spotOccupancy shouldBe 0

                coVerify(exactly = 1) {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(latitude, longitude)
                }
            }
        }

        `when`("Finding parking by coordinates successfully") {
            beforeTest {
                coEvery {
                    parkingCustomQueryRepository.findParkingByLatitudeAndLongitude(latitude, longitude)
                } returns parkingEntity
            }

            then("Should return the parking") {
                val result = parkingCustomQueryRepositoryAdapter.findParkingByCoordinates(latitude, longitude)

                result.id shouldBe expectedParking.id
                result.sector shouldBe expectedParking.sector
                result.basePrice shouldBe expectedParking.basePrice
                result.maxCapacity shouldBe expectedParking.maxCapacity

                coVerify(exactly = 1) {
                    parkingCustomQueryRepository.findParkingByLatitudeAndLongitude(latitude, longitude)
                }
            }
        }

        `when`("Finding parking by coordinates fails") {
            beforeTest {
                coEvery {
                    parkingCustomQueryRepository.findParkingByLatitudeAndLongitude(latitude, longitude)
                } throws RuntimeException("Database error")
            }

            then("Should throw ParkingNotFoundException") {
                shouldThrow<ParkingNotFoundException> {
                    parkingCustomQueryRepositoryAdapter.findParkingByCoordinates(latitude, longitude)
                }

                coVerify(exactly = 1) {
                    parkingCustomQueryRepository.findParkingByLatitudeAndLongitude(latitude, longitude)
                }
            }
        }
    }
})
