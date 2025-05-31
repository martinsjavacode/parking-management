package io.github.martinsjavacode.parkingmanagement.service.revenue

import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import java.time.LocalDate

interface GetDailyBillingByParkingSectorHandler {
    suspend fun handle(
        date: LocalDate,
        sectorName: String,
    ): Revenue
}
