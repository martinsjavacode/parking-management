package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository

import io.github.martinsjavacode.parkingmanagement.domain.model.Parking
import kotlinx.coroutines.flow.Flow

interface ParkingRepositoryPort {
    suspend fun upsert(parking: Parking)
    suspend fun findAll(): Flow<Parking>
}
