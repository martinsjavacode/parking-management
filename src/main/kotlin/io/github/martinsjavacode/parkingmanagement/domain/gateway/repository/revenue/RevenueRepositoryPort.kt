package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.revenue

import io.github.martinsjavacode.parkingmanagement.domain.model.Revenue
import java.time.LocalDate

interface RevenueRepositoryPort {
    suspend fun getRevenueForParkingOnDate(parkingId: Long, date: LocalDate): Revenue?
    suspend fun upsert(revenue: Revenue): Revenue
}
