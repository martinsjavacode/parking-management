package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository

import io.github.martinsjavacode.parkingmanagement.domain.model.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingCapacityAndOccupancy

interface ParkingCustomQueryRepositoryPort {
    suspend fun findParkingCapacityAndOccupancy(
        latitude: Double,
        longitude: Double,
    ): ParkingCapacityAndOccupancy

    suspend fun findParkingByCoordinates(
        latitude: Double,
        longitude: Double,
    ): Parking
}
