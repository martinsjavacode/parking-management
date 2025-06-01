package io.github.martinsjavacode.parkingmanagement.application.usecases.revenue

import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import java.time.LocalDate

/**
 * Interface for retrieving daily revenue by parking sector.
 *
 * Defines the contract for querying the accumulated revenue for a specific
 * day in a specific parking sector.
 */
interface GetDailyBillingByParkingSectorHandler {
    /**
     * Gets the daily revenue for the provided sector and date.
     *
     * @param date Date for querying the revenue
     * @param sector Name of the parking sector
     * @return The found revenue
     * @throws RevenueNotFoundException If no revenue is found for the sector and date
     */
    suspend fun handle(
        date: LocalDate,
        sectorName: String,
    ): Revenue
}
