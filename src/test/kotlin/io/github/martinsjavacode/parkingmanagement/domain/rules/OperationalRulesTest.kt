package io.github.martinsjavacode.parkingmanagement.domain.rules

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDateTime

class OperationalRulesTest : DescribeSpec({

    describe("OperationalRules") {

        context("priceMultiplierForOccupancyRate") {
            it("should return 0.9 when occupancy rate is less than 25%") {
                // Given
                val occupancyRate = 24

                // When
                val result = OperationalRules.priceMultiplierForOccupancyRate(occupancyRate)

                // Then
                result shouldBe 0.9
            }

            it("should return 1.0 when occupancy rate is between 25% and 49%") {
                // Given
                val lowerBound = 25
                val upperBound = 49

                // When & Then
                OperationalRules.priceMultiplierForOccupancyRate(lowerBound) shouldBe 1.0
                OperationalRules.priceMultiplierForOccupancyRate(upperBound) shouldBe 1.0
            }

            it("should return 1.1 when occupancy rate is between 50% and 74%") {
                // Given
                val lowerBound = 50
                val upperBound = 74

                // When & Then
                OperationalRules.priceMultiplierForOccupancyRate(lowerBound) shouldBe 1.1
                OperationalRules.priceMultiplierForOccupancyRate(upperBound) shouldBe 1.1
            }

            it("should return 1.25 when occupancy rate is 75% or higher") {
                // Given
                val lowerBound = 75
                val higherValue = 100

                // When & Then
                OperationalRules.priceMultiplierForOccupancyRate(lowerBound) shouldBe 1.25
                OperationalRules.priceMultiplierForOccupancyRate(higherValue) shouldBe 1.25
            }
        }

        context("assertValidCoordinates") {
            it("should not throw exception for valid coordinates") {
                // These should not throw exceptions
                OperationalRules.assertValidCoordinates(0.0, 0.0)
                OperationalRules.assertValidCoordinates(90.0, 180.0)
                OperationalRules.assertValidCoordinates(-90.0, -180.0)
                OperationalRules.assertValidCoordinates(45.5, -120.3)
            }

            it("should throw exception when latitude is null") {
                val exception =
                    shouldThrow<IllegalStateException> {
                        OperationalRules.assertValidCoordinates(null, 0.0)
                    }
                exception.message shouldBe "Latitude and longitude cannot be null"
            }

            it("should throw exception when longitude is null") {
                val exception =
                    shouldThrow<IllegalStateException> {
                        OperationalRules.assertValidCoordinates(0.0, null)
                    }
                exception.message shouldBe "Latitude and longitude cannot be null"
            }

            it("should throw exception when both coordinates are null") {
                val exception =
                    shouldThrow<IllegalStateException> {
                        OperationalRules.assertValidCoordinates(null, null)
                    }
                exception.message shouldBe "Latitude and longitude cannot be null"
            }

            it("should throw exception when latitude is out of valid range") {
                shouldThrow<IllegalStateException> {
                    OperationalRules.assertValidCoordinates(91.0, 0.0)
                }

                shouldThrow<IllegalStateException> {
                    OperationalRules.assertValidCoordinates(-91.0, 0.0)
                }
            }

            it("should throw exception when longitude is out of valid range") {
                shouldThrow<IllegalStateException> {
                    OperationalRules.assertValidCoordinates(0.0, 181.0)
                }

                shouldThrow<IllegalStateException> {
                    OperationalRules.assertValidCoordinates(0.0, -181.0)
                }
            }
        }

        context("calculateParkingFee") {
            it("should calculate correct fee for exact duration limit") {
                // Given
                val entryTime = LocalDateTime.of(2025, 1, 1, 10, 0)
                val exitTime = LocalDateTime.of(2025, 1, 1, 11, 0)
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60
                val priceMultiplier = 1.0

                // When
                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                // Then
                fee shouldBe BigDecimal("10.00")
            }

            it("should calculate correct fee for half duration limit") {
                // Given
                val entryTime = LocalDateTime.of(2025, 1, 1, 10, 0)
                val exitTime = LocalDateTime.of(2025, 1, 1, 10, 30)
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60
                val priceMultiplier = 1.0

                // When
                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                // Then
                fee shouldBe BigDecimal("5.00")
            }

            it("should calculate correct fee with price multiplier") {
                // Given
                val entryTime = LocalDateTime.of(2025, 1, 1, 10, 0)
                val exitTime = LocalDateTime.of(2025, 1, 1, 11, 0)
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60
                val priceMultiplier = 1.25

                // When
                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                // Then
                fee shouldBe BigDecimal("12.50")
            }

            it("should calculate correct fee for multiple duration periods") {
                // Given
                val entryTime = LocalDateTime.of(2025, 1, 1, 10, 0)
                val exitTime = LocalDateTime.of(2025, 1, 1, 12, 30)
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60
                val priceMultiplier = 1.0

                // When
                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                // Then
                fee shouldBe BigDecimal("25.00") // 2.5 hours = 2.5 * 10.00
            }

            it("should calculate correct fee with discount multiplier") {
                // Given
                val entryTime = LocalDateTime.of(2025, 1, 1, 10, 0)
                val exitTime = LocalDateTime.of(2025, 1, 1, 11, 0)
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60
                val priceMultiplier = 0.9

                // When
                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                // Then
                fee shouldBe BigDecimal("9.00")
            }
        }
    }
})
