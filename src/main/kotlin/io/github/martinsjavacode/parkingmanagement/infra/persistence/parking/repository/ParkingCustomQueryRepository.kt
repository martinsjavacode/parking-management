package io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingCapacityAndOccupancy
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEntity

interface ParkingCustomQueryRepository {
    suspend fun findParkingByLatitudeAndLongitude(
        latitude: Double,
        longitude: Double,
    ): ParkingEntity?

    suspend fun findParkingCapacityAndOccupancy(
        latitude: Double,
        longitude: Double,
    ): ParkingCapacityAndOccupancy?
}
