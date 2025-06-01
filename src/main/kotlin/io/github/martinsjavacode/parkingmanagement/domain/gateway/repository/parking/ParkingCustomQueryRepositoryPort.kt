package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingCapacityAndOccupancy

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
