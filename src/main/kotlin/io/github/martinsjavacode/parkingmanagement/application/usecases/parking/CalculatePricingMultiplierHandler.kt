package io.github.martinsjavacode.parkingmanagement.application.usecases.parking

/**
 * Interface for dynamic pricing multiplier calculation.
 *
 * Defines the contract for calculating a pricing multiplier based on
 * parking lot occupancy at specified coordinates.
 */
interface CalculatePricingMultiplierHandler {
    /**
     * Calculates the pricing multiplier for the given coordinates.
     *
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return The calculated pricing multiplier
     */
    suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): Double
}
