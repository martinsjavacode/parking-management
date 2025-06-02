package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import kotlinx.coroutines.flow.Flow

/**
 * Port interface for parking repository operations.
 *
 * This interface defines the contract for repository operations related to parking entities,
 * following the hexagonal architecture pattern.
 */
interface ParkingRepositoryPort {
    /**
     * Inserts or updates a parking entity.
     *
     * @param parking The parking entity to be saved or updated
     */
    suspend fun upsert(parking: Parking)

    /**
     * Retrieves all parking entities.
     *
     * @return A flow of all parking entities
     */
    suspend fun findAll(): Flow<Parking>

    /**
     * Finds a parking entity by its sector name.
     *
     * @param sector The sector name to search for
     * @return The parking entity with the specified sector name
     * @throws ParkingNotFoundException if no parking with the given sector name is found
     */
    suspend fun findBySectorName(sector: String): Parking
}
