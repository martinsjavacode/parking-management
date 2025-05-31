package io.github.martinsjavacode.parkingmanagement.service.parking.impl

import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.service.parking.GetParkingByCoordinatesOrThrowHandler
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service

@Service
class GetParkingByCoordinatesOrThrowHandlerImpl(
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort,
) : GetParkingByCoordinatesOrThrowHandler {
    private val locale = LocaleContextHolder.getLocale()

    override suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): Parking = parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
}
