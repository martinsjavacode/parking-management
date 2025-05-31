package io.github.martinsjavacode.parkingmanagement.application.usecases.plate

import io.github.martinsjavacode.parkingmanagement.domain.model.vehicle.PlateStatus

interface GetPlateStatusHandler {
    suspend fun handle(licensePlate: String): PlateStatus
}
