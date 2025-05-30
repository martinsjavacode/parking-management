package io.github.martinsjavacode.parkingmanagement.infra.persistence.repository

import io.github.martinsjavacode.parkingmanagement.infra.persistence.entity.ParkingSpotEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ParkingSpotRepository : CoroutineCrudRepository<ParkingSpotEntity, Long> {
    suspend fun findByLatitudeAndLongitude(
        latitude: Double,
        longitude: Double,
    ): ParkingSpotEntity
}
