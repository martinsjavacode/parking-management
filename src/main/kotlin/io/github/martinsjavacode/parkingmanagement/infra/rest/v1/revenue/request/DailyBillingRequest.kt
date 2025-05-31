package io.github.martinsjavacode.parkingmanagement.infra.rest.v1.revenue.request

import java.time.LocalDate

data class DailyBillingRequest(
    val date: LocalDate,
    val sector: String,
)
