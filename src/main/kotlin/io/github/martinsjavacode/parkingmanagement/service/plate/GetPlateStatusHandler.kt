package io.github.martinsjavacode.parkingmanagement.service.plate

import io.github.martinsjavacode.parkingmanagement.domain.model.vehicle.PlateStatus

interface GetPlateStatusHandler {
    suspend fun handle(licensePlate: String): PlateStatus
}
