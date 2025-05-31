package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpot

interface ParkingSpotRepositoryPort {
    suspend fun findByCoordinates(
        latitude: Double,
        longitude: Double,
    ): ParkingSpot
    suspend fun save(parkingSpot: ParkingSpot): ParkingSpot
}
