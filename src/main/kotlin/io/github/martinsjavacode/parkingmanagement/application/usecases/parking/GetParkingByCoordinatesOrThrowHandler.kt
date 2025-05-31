package io.github.martinsjavacode.parkingmanagement.application.usecases.parking

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking

interface GetParkingByCoordinatesOrThrowHandler {
    suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): Parking
}
