package io.github.martinsjavacode.parkingmanagement.infra.persistence.repository

import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingCapacityAndOccupancy
import io.github.martinsjavacode.parkingmanagement.infra.persistence.entity.ParkingEntity


interface ParkingCustomQueryRepository {
    suspend fun findParkingByLatitudeAndLongitude(latitude: Double, longitude: Double): ParkingEntity?
    suspend fun findParkingCapacityAndOccupancy(latitude: Double, longitude: Double): ParkingCapacityAndOccupancy?
}
