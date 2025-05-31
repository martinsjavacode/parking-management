package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.revenue.request

import java.time.LocalDate

data class DailyBillingRequest(
    val date: LocalDate,
    val sector: String,
)
