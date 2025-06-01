package io.github.martinsjavacode.parkingmanagement.application.usecases.parking.impl

import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingCapacityAndOccupancy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

class CalculatePricingMultiplierHandlerImplTest : DescribeSpec({

    val parkingCustomQueryRepository = mockk<ParkingCustomQueryRepositoryPort>()
    val handler = CalculatePricingMultiplierHandlerImpl(parkingCustomQueryRepository)

    describe("CalculatePricingMultiplierHandler") {
        val validLatitude = 10.123456
        val validLongitude = -20.654321

        context("when calculating price multiplier") {
            it("should return 0.9 when occupancy rate is less than 25%") {
                // Given
                coEvery {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(validLatitude, validLongitude)
                } returns ParkingCapacityAndOccupancy(maxCapacity = 100, spotOccupancy = 20)

                // When
                val result = handler.handle(validLatitude, validLongitude)

                // Then
                result shouldBe 0.9
            }

            it("should return 1.0 when occupancy rate is between 25% and 49%") {
                // Given
                coEvery {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(validLatitude, validLongitude)
                } returns ParkingCapacityAndOccupancy(maxCapacity = 100, spotOccupancy = 35)

                // When
                val result = handler.handle(validLatitude, validLongitude)

                // Then
                result shouldBe 1.0
            }

            it("should return 1.1 when occupancy rate is between 50% and 74%") {
                // Given
                coEvery {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(validLatitude, validLongitude)
                } returns ParkingCapacityAndOccupancy(maxCapacity = 100, spotOccupancy = 70)

                // When
                val result = handler.handle(validLatitude, validLongitude)

                // Then
                result shouldBe 1.1
            }

            it("should return 1.25 when occupancy rate is 75% or higher") {
                // Given
                coEvery {
                    parkingCustomQueryRepository.findParkingCapacityAndOccupancy(validLatitude, validLongitude)
                } returns ParkingCapacityAndOccupancy(maxCapacity = 100, spotOccupancy = 95)

                // When
                val result = handler.handle(validLatitude, validLongitude)

                // Then
                result shouldBe 1.25
            }
        }

        context("when validating coordinates") {
            it("should throw exception for invalid latitude") {
                // Given
                val invalidLatitude = 100.0

                // When/Then
                shouldThrow<IllegalStateException> {
                    handler.handle(invalidLatitude, validLongitude)
                }
            }

            it("should throw exception for invalid longitude") {
                // Given
                val invalidLongitude = 200.0

                // When/Then
                shouldThrow<IllegalStateException> {
                    handler.handle(validLatitude, invalidLongitude)
                }
            }
        }
    }
})
