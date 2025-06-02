package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.revenue

import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import java.time.LocalDate

/**
 * Port interface for revenue repository operations.
 *
 * This interface defines the contract for repository operations related to revenue entities,
 * following the hexagonal architecture pattern.
 */
interface RevenueRepositoryPort {
    /**
     * Retrieves the revenue for a specific parking lot on a given date.
     *
     * @param parkingId The ID of the parking lot
     * @param date The date for which to retrieve revenue
     * @return The revenue record, or null if none exists
     */
    suspend fun getRevenueForParkingOnDate(
        parkingId: Long,
        date: LocalDate,
    ): Revenue?

    /**
     * Inserts or updates a revenue entity.
     *
     * @param revenue The revenue entity to be saved or updated
     * @return The saved revenue entity with generated ID
     */
    suspend fun upsert(revenue: Revenue): Revenue
}
