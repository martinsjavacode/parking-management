package io.github.martinsjavacode.parkingmanagement.infra.persistence.parking

import io.github.martinsjavacode.parkingmanagement.domain.extension.parking.toDomain
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingSpotRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.infra.persistence.handler.PersistenceHandler
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingSpotRepository
import io.github.martinsjavacode.parkingmanagement.loggerFor
import org.springframework.stereotype.Component

@Component
class ParkingSpotRepositoryAdapter(
    private val parkingSpotRepository: ParkingSpotRepository,
    private val persistenceHandler: PersistenceHandler,
) : ParkingSpotRepositoryPort {
    private val logger = loggerFor<ParkingSpotRepositoryAdapter>()

    override suspend fun findByCoordinates(
        latitude: Double,
        longitude: Double,
    ): ParkingSpot =
        persistenceHandler.handleOperation {
            val parkingSpotEntity = parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
            parkingSpotEntity.toDomain()
        }
}
