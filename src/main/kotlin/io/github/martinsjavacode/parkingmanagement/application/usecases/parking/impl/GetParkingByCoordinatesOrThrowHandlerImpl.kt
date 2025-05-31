package io.github.martinsjavacode.parkingmanagement.application.usecases.parking.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetParkingByCoordinatesOrThrowHandler
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import org.springframework.stereotype.Service

@Service
class GetParkingByCoordinatesOrThrowHandlerImpl(
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort,
) : GetParkingByCoordinatesOrThrowHandler {
    override suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): Parking = parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
}
