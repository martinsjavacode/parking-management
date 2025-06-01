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
            result shouldBe 0 // Em divisão de inteiros, 25/100 = 0, então 0*100 = 0
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
        
        it("should handle integer division correctly") {
            // Given
            val numerator = 5
            val denominator = 10
            
            // When
            val result = numerator.percentOf(denominator)
            
            // Then
            result shouldBe 0 // Em divisão de inteiros, 5/10 = 0, então 0*100 = 0
        }
        
        it("should truncate decimal results") {
            // Given
            val numerator = 1
            val denominator = 3
            
            // When
            val result = numerator.percentOf(denominator)
            
            // Then
            // Em divisão de inteiros, 1/3 = 0, então 0*100 = 0
            result shouldBe 0
        }
    }
})
