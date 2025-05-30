package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.revenue

import io.github.martinsjavacode.parkingmanagement.domain.model.Revenue

interface RevenueRepositoryPort {
    suspend fun findDailyRevenueByParkingId(parkingId: Long): Revenue?

    suspend fun upsert(revenue: Revenue): Revenue
}
