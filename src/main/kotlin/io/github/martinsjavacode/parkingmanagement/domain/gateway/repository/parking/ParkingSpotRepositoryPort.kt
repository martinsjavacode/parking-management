package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpot

/**
 * Port interface for parking spot repository operations.
 *
 * This interface defines the contract for repository operations related to parking spot entities,
 * following the hexagonal architecture pattern.
 */
interface ParkingSpotRepositoryPort {
    /**
     * Finds a parking spot by its geographical coordinates.
     *
     * @param latitude The latitude coordinate to search for
     * @param longitude The longitude coordinate to search for
     * @return The parking spot at the specified coordinates
     * @throws ParkingSpotNotFoundException if no spot is found at the coordinates
     */
    suspend fun findByCoordinates(
        latitude: Double,
        longitude: Double,
    ): ParkingSpot

    /**
     * Saves a parking spot entity.
     *
     * @param parkingSpot The parking spot to be saved
     * @return The saved parking spot with generated ID
     */
    suspend fun save(parkingSpot: ParkingSpot): ParkingSpot
}
