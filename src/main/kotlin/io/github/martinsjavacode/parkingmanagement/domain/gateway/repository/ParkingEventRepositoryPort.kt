package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository

import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingEvent

interface ParkingEventRepositoryPort {
    suspend fun save(parkingEvent: ParkingEvent)

    suspend fun findByLicensePlate(licensePlate: String): ParkingEvent
}
