package io.github.martinsjavacode.parkingmanagement.service.revenue

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import java.math.BigDecimal

interface UpdateOrInitializeDailyRevenueHandler {
    suspend fun handle(
        eventType: EventType,
        latitude: Double,
        longitude: Double,
        amountPaid: BigDecimal = BigDecimal.ZERO,
    ): Revenue?
}
