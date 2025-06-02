package io.github.martinsjavacode.parkingmanagement.domain.gateway.client

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import kotlinx.coroutines.flow.Flow

/**
 * Port interface for external parking API operations.
 *
 * This interface defines the contract for interacting with external parking APIs,
 * following the hexagonal architecture pattern.
 */
interface ExternalParkingApiPort {
    /**
     * Fetches garage configuration data from an external API.
     *
     * @return A flow of parking entities representing garage configurations
     */
    suspend fun fetchGarageConfig(): Flow<Parking>
}
