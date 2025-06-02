package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.revenue.request

import com.fasterxml.jackson.annotation.JsonAutoDetect
import java.time.LocalDate

@JsonAutoDetect
data class DailyBillingRequest(
    val date: LocalDate,
    val sector: String,
)
