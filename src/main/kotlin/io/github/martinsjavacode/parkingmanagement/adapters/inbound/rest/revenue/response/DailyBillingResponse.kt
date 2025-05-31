package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.revenue.response

import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import java.math.BigDecimal
import java.time.LocalDateTime

data class DailyBillingResponse(
    val amount: BigDecimal,
    val currency: CurrencyType,
    val timestamp: LocalDateTime,
)
