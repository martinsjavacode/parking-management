package io.github.martinsjavacode.parkingmanagement.infra.rest.v1.revenue.response

import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import java.math.BigDecimal
import java.time.LocalDateTime

data class DailyBillingResponse(
    val amount: BigDecimal,
    val currency: CurrencyType,
    val timestamp: LocalDateTime,
)
