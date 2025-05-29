package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository

import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingSpot

interface ParkingSpotRepositoryPort {
    suspend fun findByCoordinates(
        latitude: Double,
        longitude: Double,
    ): ParkingSpot
}
