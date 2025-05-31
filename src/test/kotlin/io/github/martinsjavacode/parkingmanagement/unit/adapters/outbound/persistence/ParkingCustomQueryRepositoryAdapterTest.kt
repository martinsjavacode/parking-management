package io.github.martinsjavacode.parkingmanagement.unit.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence.ParkingCustomQueryRepositoryAdapter
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingCapacityAndOccupancy
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEntity
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingCustomQueryRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.LocalTime

class ParkingCustomQueryRepositoryAdapterTest : DescribeSpec({

    val parkingCustomQueryRepository = mockk<ParkingCustomQueryRepository>()
    val messageSource = mockk<MessageSource>()
    val traceContext = mockk<TraceContext>()

    val adapter =
        ParkingCustomQueryRepositoryAdapter(
            messageSource = messageSource,
            traceContext = traceContext,
            parkingCustomQueryRepository = parkingCustomQueryRepository,
        )

    beforeTest {
        clearAllMocks()

        // Common mocks
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
        every { traceContext.traceId() } returns "test-trace-id"
    }

    describe("ParkingCustomQueryRepositoryAdapter") {

        context("findParkingCapacityAndOccupancy") {
            it("should return parking capacity and occupancy") {
                // Given
                val latitude = 45.0
                val longitude = 90.0
                val capacityAndOccupancy =
                    ParkingCapacityAndOccupancy(
                        maxCapacity = 100,
                        spotOccupancy = 75,
                    )

                coEvery {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(latitude, longitude)
                } returns capacityAndOccupancy

                // When
                val result = adapter.findParkingCapacityAndOccupancy(latitude, longitude)

                // Then
                result.maxCapacity shouldBe 100
                result.spotOccupancy shouldBe 75

                coVerify(exactly = 1) {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(latitude, longitude)
                }
            }

            it("should return default values when query returns null") {
                // Given
                val latitude = 45.0
                val longitude = 90.0

                coEvery {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(latitude, longitude)
                } returns null

                // When
                val result = adapter.findParkingCapacityAndOccupancy(latitude, longitude)

                // Then
                result.maxCapacity shouldBe 1
                result.spotOccupancy shouldBe 0

                coVerify(exactly = 1) {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(latitude, longitude)
                }
            }
        }

        context("findParkingByCoordinates") {
            it("should return parking by coordinates") {
                // Given
                val latitude = 45.0
                val longitude = 90.0
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

                coEvery {
                    parkingCustomQueryRepository.findParkingByLatitudeAndLongitude(latitude, longitude)
                } returns parkingEntity

                // When
                val result = adapter.findParkingByCoordinates(latitude, longitude)

                // Then
                result.id shouldBe 1L
                result.sector shouldBe "A"
                result.basePrice shouldBe BigDecimal("10.00")

                coVerify(exactly = 1) {
                    parkingCustomQueryRepository.findParkingByLatitudeAndLongitude(latitude, longitude)
                }
            }

            it("should throw ParkingNotFoundException when parking not found") {
                // Given
                val latitude = 45.0
                val longitude = 90.0

                coEvery {
                    parkingCustomQueryRepository.findParkingByLatitudeAndLongitude(latitude, longitude)
                } throws RuntimeException("Not found")

                // When/Then
                shouldThrow<ParkingNotFoundException> {
                    adapter.findParkingByCoordinates(latitude, longitude)
                }

                coVerify(exactly = 1) {
                    parkingCustomQueryRepository.findParkingByLatitudeAndLongitude(latitude, longitude)
                }
            }
        }
    }
})
