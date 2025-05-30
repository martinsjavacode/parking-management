package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import kotlinx.coroutines.flow.Flow

interface ParkingEventRepositoryPort {
    suspend fun save(parkingEvent: ParkingEvent)

    suspend fun findAllByLicensePlate(licensePlate: String): Flow<ParkingEvent>

    suspend fun findActiveParkingEventByLicensePlate(
        licensePlate: String,
        latitude: Double,
        longitude: Double,
    ): Flow<ParkingEvent>
}
