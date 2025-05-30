package io.github.martinsjavacode.parkingmanagement.domain.model

import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import java.math.BigDecimal
import java.time.LocalDate

data class Revenue(
    val id: Long? = null,
    val parkingId: Long,
    val date: LocalDate,
    val amount: BigDecimal,
    val currency: CurrencyType,
)
