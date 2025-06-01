package io.github.martinsjavacode.parkingmanagement.domain.gateway.client

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import kotlinx.coroutines.flow.Flow

interface ExternalParkingApiPort {
    suspend fun fetchGarageConfig(): Flow<Parking>
}
