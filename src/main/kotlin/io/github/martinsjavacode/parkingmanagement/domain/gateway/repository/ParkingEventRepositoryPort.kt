package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository

import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingEvent
import kotlinx.coroutines.flow.Flow

interface ParkingEventRepositoryPort {
    suspend fun save(parkingEvent: ParkingEvent)
    suspend fun findByLicensePlate(licensePlate: String): ParkingEvent
    suspend fun findActiveParkingEventByLicensePlate(
        licensePlate: String,
        latitude: Double,
        longitude: Double
    ): Flow<ParkingEvent>
}
