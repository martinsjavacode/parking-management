package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import kotlinx.coroutines.flow.Flow

interface ParkingRepositoryPort {
    suspend fun upsert(parking: Parking)

    suspend fun findAll(): Flow<Parking>
}
