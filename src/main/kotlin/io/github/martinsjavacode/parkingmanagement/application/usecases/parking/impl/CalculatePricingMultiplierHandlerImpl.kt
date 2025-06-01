package io.github.martinsjavacode.parkingmanagement.application.usecases.parking.impl

import io.github.martinsjavacode.parkingmanagement.adapters.extension.percentOf
import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.CalculatePricingMultiplierHandler
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import org.springframework.stereotype.Service

/**
 * Implementation of the handler for dynamic pricing multiplier calculation.
 *
 * This class calculates the pricing multiplier based on the parking lot occupancy rate
 * at the given coordinates.
 *
 * @property parkingCustomQueryRepository Repository for custom parking queries
 */
@Service
class CalculatePricingMultiplierHandlerImpl(
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort,
) : CalculatePricingMultiplierHandler {
    /**
     * Calculates the pricing multiplier for the given coordinates.
     *
     * First validates the coordinates, then computes the occupancy rate,
     * and finally determines the pricing multiplier based on that rate.
     *
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return The calculated pricing multiplier
     */
    override suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): Double {
        OperationalRules.assertValidCoordinates(latitude, longitude)

        val occupancyRate = calculateOccupancyRate(latitude, longitude)
        return OperationalRules.priceMultiplierForOccupancyRate(occupancyRate)
    }

    /**
     * Calculates the occupancy rate of the parking lot at the given coordinates.
     *
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return The occupancy rate as a percentage (0-100)
     */
    private suspend fun calculateOccupancyRate(
        latitude: Double,
        longitude: Double,
    ): Int {
        val (maxCapacity, spotOccupancy) =
            parkingCustomQueryRepository.findParkingCapacityAndOccupancy(
                latitude,
                longitude,
            )

        return spotOccupancy.percentOf(maxCapacity)
    }
}
