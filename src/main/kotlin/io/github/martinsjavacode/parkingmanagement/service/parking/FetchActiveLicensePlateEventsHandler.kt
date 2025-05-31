package io.github.martinsjavacode.parkingmanagement.service.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import kotlinx.coroutines.flow.Flow

interface FetchActiveLicensePlateEventsHandler {
    suspend fun handle(
        licensePlate: String,
        latitude: Double?,
        longitude: Double?,
    ): Flow<ParkingEvent>
}
