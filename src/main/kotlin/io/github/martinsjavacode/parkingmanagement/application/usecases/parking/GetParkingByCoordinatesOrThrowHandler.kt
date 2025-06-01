package io.github.martinsjavacode.parkingmanagement.application.usecases.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking

/**
 * Interface for retrieving parking information by coordinates.
 *
 * Defines the contract for fetching a parking lot based on geographic coordinates,
 * throwing an exception if none is found.
 */
interface GetParkingByCoordinatesOrThrowHandler {
    /**
     * Fetches a parking lot based on the given coordinates.
     *
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return The found parking lot
     * @throws ParkingNotFoundException If no parking lot is found at the coordinates
     */
    suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): Parking
}
