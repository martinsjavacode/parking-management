package io.github.martinsjavacode.parkingmanagement.application.usecases.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent

interface GetMostRecentParkingEvent {
    suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): ParkingEvent
}
