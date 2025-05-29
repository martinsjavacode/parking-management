package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository

import io.github.martinsjavacode.parkingmanagement.domain.model.Parking

interface ParkingRepositoryPort {
    suspend fun upsert(parking: Parking)

    suspend fun findBySector(sector: String): Parking
}
