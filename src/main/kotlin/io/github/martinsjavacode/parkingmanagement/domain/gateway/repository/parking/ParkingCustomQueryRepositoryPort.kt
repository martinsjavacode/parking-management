package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingCapacityAndOccupancy

/**
 * Port interface for custom parking repository queries.
 *
 * This interface defines the contract for specialized repository operations
 * that require custom queries beyond standard CRUD operations.
 */
interface ParkingCustomQueryRepositoryPort {
    /**
     * Finds the capacity and current occupancy of a parking lot based on coordinates.
     *
     * @param latitude The latitude coordinate to search near
     * @param longitude The longitude coordinate to search near
     * @return The capacity and occupancy information for the parking lot
     * @throws ParkingNotFoundException if no parking is found near the coordinates
     */
    suspend fun findParkingCapacityAndOccupancy(
        latitude: Double,
        longitude: Double,
    ): ParkingCapacityAndOccupancy

    /**
     * Finds a parking lot by geographical coordinates.
     *
     * @param latitude The latitude coordinate to search near
     * @param longitude The longitude coordinate to search near
     * @return The parking lot near the specified coordinates
     * @throws ParkingNotFoundException if no parking is found near the coordinates
     */
    suspend fun findParkingByCoordinates(
        latitude: Double,
        longitude: Double,
    ): Parking
}
