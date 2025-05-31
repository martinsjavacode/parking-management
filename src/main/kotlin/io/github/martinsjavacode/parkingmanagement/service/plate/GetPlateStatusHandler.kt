package io.github.martinsjavacode.parkingmanagement.service.plate

interface GetPlateStatusHandler {
    suspend fun handle(licensePlate: String)
}
