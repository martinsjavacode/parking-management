package io.github.martinsjavacode.parkingmanagement.application.usecases.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpotStatus

/**
 * Interface for retrieving the status of a parking spot.
 *
 * Defines the contract for checking the status of a parking spot
 * based on its geographic coordinates.
 */
interface GetParkingSpotStatusHandler {
    /**
     * Gets the status of a parking spot at the given coordinates.
     *
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return The status of the parking spot
     */
    suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): ParkingSpotStatus
}
