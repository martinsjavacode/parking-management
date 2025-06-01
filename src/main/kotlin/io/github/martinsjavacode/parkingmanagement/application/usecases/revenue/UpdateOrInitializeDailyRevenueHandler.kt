package io.github.martinsjavacode.parkingmanagement.application.usecases.revenue

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import java.math.BigDecimal

/**
 * Interface for updating or initializing daily revenue.
 *
 * Defines the contract for updating the daily revenue of a parking lot
 * or initializing it if it does not yet exist.
 */
interface UpdateOrInitializeDailyRevenueHandler {
    /**
     * Updates or initializes the daily revenue for the parking lot at the given coordinates.
     *
     * @param eventType Type of event triggering the update
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param amountPaid Amount paid, used only for exit events
     * @return The updated revenue
     */
    suspend fun handle(
        eventType: EventType,
        latitude: Double,
        longitude: Double,
        amountPaid: BigDecimal = BigDecimal.ZERO,
    ): Revenue?
}
