package io.github.martinsjavacode.parkingmanagement.infra.persistence.revenue.entity

import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate

@Table("revenues")
data class RevenueEntity(
    @Id val id: Long? = null,
    @Column("parking_id")
    val parkingId: Long,
    val date: LocalDate,
    val amount: BigDecimal,
    val currency: CurrencyType,
)
