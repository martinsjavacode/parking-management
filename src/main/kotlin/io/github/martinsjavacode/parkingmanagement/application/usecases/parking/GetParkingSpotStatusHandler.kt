package io.github.martinsjavacode.parkingmanagement.application.usecases.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpotStatus

interface GetParkingSpotStatusHandler {
    suspend fun handle(latitude: Double, longitude: Double): ParkingSpotStatus
}
