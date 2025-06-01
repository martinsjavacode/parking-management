package io.github.martinsjavacode.parkingmanagement.adapters.extension.revenue

import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import io.github.martinsjavacode.parkingmanagement.infra.persistence.revenue.entity.RevenueEntity
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class RevenueExtensionTest : DescribeSpec({
    describe("RevenueExtension") {
        describe("Revenue.toEntity") {
            it("should convert Revenue domain model to RevenueEntity") {
                // Given
                val today = LocalDate.now()
                val revenue = Revenue(
                    id = 1L,
                    parkingId = 10L,
                    date = today,
                    amount = BigDecimal("150.00"),
                    currency = CurrencyType.BRL
                )
                
                // When
                val result = revenue.toEntity()
                
                // Then
                result.id shouldBe 1L
                result.parkingId shouldBe 10L
                result.date shouldBe today
                result.amount shouldBe BigDecimal("150.00")
                result.currency shouldBe CurrencyType.BRL
            }
        }
        
        describe("RevenueEntity.toDomain") {
            it("should convert RevenueEntity to Revenue domain model") {
                // Given
                val yesterday = LocalDate.now().minusDays(1)
                val revenueEntity = RevenueEntity(
                    id = 2L,
                    parkingId = 20L,
                    date = yesterday,
                    amount = BigDecimal("250.00"),
                    currency = CurrencyType.BRL
                )
                
                // When
                val result = revenueEntity.toDomain()
                
                // Then
                result.id shouldBe 2L
                result.parkingId shouldBe 20L
                result.date shouldBe yesterday
                result.amount shouldBe BigDecimal("250.00")
                result.currency shouldBe CurrencyType.BRL
            }
        }
    }
})
