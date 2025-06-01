package io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository

import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingSpotEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ParkingSpotRepository : CoroutineCrudRepository<ParkingSpotEntity, Long> {
    suspend fun findByLatitudeAndLongitude(
        latitude: Double,
        longitude: Double,
    ): ParkingSpotEntity
}
