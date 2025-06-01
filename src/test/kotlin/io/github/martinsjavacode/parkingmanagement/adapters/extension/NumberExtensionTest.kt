package io.github.martinsjavacode.parkingmanagement.adapters.extension

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class NumberExtensionTest : DescribeSpec({
    describe("Int.percentOf extension function") {
        it("should calculate the correct percentage") {
            // Given
            val numerator = 25
            val denominator = 100

            // When
            val result = numerator.percentOf(denominator)

            // Then
            result shouldBe 25
        }

        it("should handle zero values correctly") {
            // Given
            val numerator = 0
            val denominator = 100

            // When
            val result = numerator.percentOf(denominator)

            // Then
            result shouldBe 0
        }
    }
})
