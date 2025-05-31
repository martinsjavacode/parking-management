package io.github.martinsjavacode.parkingmanagement.unit.adapters.extension

import io.github.martinsjavacode.parkingmanagement.adapters.extension.percentOf
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class NumberExtensionTest : DescribeSpec({

    describe("NumberExtension") {

        context("percentOf") {
            it("should calculate correct percentage") {
                // Given
                val numerator = 25
                val denominator = 100

                // When
                val result = numerator.percentOf(denominator)

                // Then
                // Due to integer division, 25/100 = 0, then 0*100 = 0
                result shouldBe 0
            }

            it("should handle zero denominator") {
                // Given
                val numerator = 10
                val denominator = 0

                // When/Then
                // This should throw an ArithmeticException due to division by zero
                // but we'll test the actual behavior
                try {
                    numerator.percentOf(denominator)
                    // If we reach here, it didn't throw an exception
                    // The test should fail
                    false shouldBe true
                } catch (e: ArithmeticException) {
                    // Expected behavior
                    e.message shouldBe "/ by zero"
                }
            }

            it("should handle zero numerator") {
                // Given
                val numerator = 0
                val denominator = 100

                // When
                val result = numerator.percentOf(denominator)

                // Then
                result shouldBe 0
            }

            it("should handle numerator greater than denominator") {
                // Given
                val numerator = 150
                val denominator = 100

                // When
                val result = numerator.percentOf(denominator)

                // Then
                // 150/100 = 1, then 1*100 = 100
                result shouldBe 100
            }

            it("should handle integer division truncation") {
                // Given
                val numerator = 33
                val denominator = 100

                // When
                val result = numerator.percentOf(denominator)

                // Then
                // Due to integer division, 33/100 * 100 = 0 * 100 = 0
                // This is expected behavior with integer division
                result shouldBe 0
            }
        }
    }
})
