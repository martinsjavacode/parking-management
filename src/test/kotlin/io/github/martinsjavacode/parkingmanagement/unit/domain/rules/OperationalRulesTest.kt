package io.github.martinsjavacode.parkingmanagement.unit.domain.rules

import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDateTime

class OperationalRulesTest : DescribeSpec({

    describe("OperationalRules") {

        context("priceMultiplierForOccupancyRate") {
            it("should return 0.9 for occupancy rate less than 25%") {
                forAll(
                    row(0),
                    row(10),
                    row(24),
                ) { occupancyRate ->
                    OperationalRules.priceMultiplierForOccupancyRate(occupancyRate) shouldBe 0.9
                }
            }

            it("should return 1.0 for occupancy rate between 25% and 49%") {
                forAll(
                    row(25),
                    row(35),
                    row(49),
                ) { occupancyRate ->
                    OperationalRules.priceMultiplierForOccupancyRate(occupancyRate) shouldBe 1.0
                }
            }

            it("should return 1.1 for occupancy rate between 50% and 74%") {
                forAll(
                    row(50),
                    row(60),
                    row(74),
                ) { occupancyRate ->
                    OperationalRules.priceMultiplierForOccupancyRate(occupancyRate) shouldBe 1.1
                }
            }

            it("should return 1.25 for occupancy rate 75% or higher") {
                forAll(
                    row(75),
                    row(90),
                    row(100),
                ) { occupancyRate ->
                    OperationalRules.priceMultiplierForOccupancyRate(occupancyRate) shouldBe 1.25
                }
            }
        }

        context("assertValidCoordinates") {
            it("should not throw exception for valid coordinates") {
                // This should not throw any exception
                OperationalRules.assertValidCoordinates(45.0, 90.0)
            }

            it("should accept boundary values") {
                // These should not throw any exception
                OperationalRules.assertValidCoordinates(-90.0, -180.0)
                OperationalRules.assertValidCoordinates(90.0, 180.0)
                OperationalRules.assertValidCoordinates(0.0, 0.0)
            }

            it("should throw exception for null latitude") {
                val exception =
                    shouldThrow<IllegalStateException> {
                        OperationalRules.assertValidCoordinates(null, 90.0)
                    }
                exception.message shouldBe "Invalid latitude or longitude"
            }

            it("should throw exception for null longitude") {
                val exception =
                    shouldThrow<IllegalStateException> {
                        OperationalRules.assertValidCoordinates(45.0, null)
                    }
                exception.message shouldBe "Invalid latitude or longitude"
            }

            it("should throw exception for invalid latitude") {
                forAll(
                    row(-91.0),
                    row(91.0),
                    row(Double.MAX_VALUE),
                ) { invalidLatitude ->
                    val exception =
                        shouldThrow<IllegalStateException> {
                            OperationalRules.assertValidCoordinates(invalidLatitude, 90.0)
                        }
                    exception.message shouldBe "Invalid latitude or longitude"
                }
            }

            it("should throw exception for invalid longitude") {
                forAll(
                    row(-181.0),
                    row(181.0),
                    row(Double.MAX_VALUE),
                ) { invalidLongitude ->
                    val exception =
                        shouldThrow<IllegalStateException> {
                            OperationalRules.assertValidCoordinates(45.0, invalidLongitude)
                        }
                    exception.message shouldBe "Invalid latitude or longitude"
                }
            }
        }

        context("calculateParkingFee") {
            it("should calculate correct fee for one hour parking with base price") {
                val entryTime = LocalDateTime.of(2025, 1, 1, 10, 0)
                val exitTime = LocalDateTime.of(2025, 1, 1, 11, 0)
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60
                val priceMultiplier = 1.0

                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                fee shouldBe BigDecimal("10.00")
            }

            it("should calculate correct fee for half hour parking") {
                val entryTime = LocalDateTime.of(2025, 1, 1, 10, 0)
                val exitTime = LocalDateTime.of(2025, 1, 1, 10, 30)
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60
                val priceMultiplier = 1.0

                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                fee shouldBe BigDecimal("5.00")
            }

            it("should apply discount multiplier correctly") {
                val entryTime = LocalDateTime.of(2025, 1, 1, 10, 0)
                val exitTime = LocalDateTime.of(2025, 1, 1, 11, 0)
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60
                val priceMultiplier = 0.9

                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                fee shouldBe BigDecimal("9.00")
            }

            it("should apply surcharge multiplier correctly") {
                val entryTime = LocalDateTime.of(2025, 1, 1, 10, 0)
                val exitTime = LocalDateTime.of(2025, 1, 1, 11, 0)
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60
                val priceMultiplier = 1.25

                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                fee shouldBe BigDecimal("12.50")
            }

            it("should handle longer duration than limit") {
                val entryTime = LocalDateTime.of(2025, 1, 1, 10, 0)
                val exitTime = LocalDateTime.of(2025, 1, 1, 12, 0) // 2 hours
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60 // 1 hour limit
                val priceMultiplier = 1.0

                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                fee shouldBe BigDecimal("20.00")
            }

            it("should handle overnight parking correctly") {
                val entryTime = LocalDateTime.of(2025, 1, 1, 22, 0)
                val exitTime = LocalDateTime.of(2025, 1, 2, 6, 0) // 8 hours overnight
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60
                val priceMultiplier = 1.0

                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                fee shouldBe BigDecimal("80.00")
            }

            it("should round to two decimal places") {
                val entryTime = LocalDateTime.of(2025, 1, 1, 10, 0)
                val exitTime = LocalDateTime.of(2025, 1, 1, 10, 20) // 20 minutes = 1/3 of an hour
                val basePrice = BigDecimal("10.00")
                val durationLimitMinutes = 60
                val priceMultiplier = 1.0

                val fee =
                    OperationalRules.calculateParkingFee(
                        entryTime,
                        exitTime,
                        basePrice,
                        durationLimitMinutes,
                        priceMultiplier,
                    )

                fee shouldBe BigDecimal("3.33")
            }
        }
    }
})
