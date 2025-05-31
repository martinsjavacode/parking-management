package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import kotlinx.coroutines.flow.Flow

interface ParkingEventRepositoryPort {
    suspend fun save(parkingEvent: ParkingEvent)

    suspend fun findAllByLicensePlate(licensePlate: String): Flow<ParkingEvent>

    suspend fun findLastParkingEventByLicenseAndEventType(
        licensePlate: String,
        eventType: EventType,
    ): ParkingEvent
}
