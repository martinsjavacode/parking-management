package io.github.martinsjavacode.parkingmanagement.service.parking

interface ParkingSyncHandler {
    suspend fun handle()
}
