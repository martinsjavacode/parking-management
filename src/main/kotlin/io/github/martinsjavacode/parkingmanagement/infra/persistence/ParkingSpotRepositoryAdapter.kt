package io.github.martinsjavacode.parkingmanagement.infra.persistence

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.extension.toDomain
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingSpotRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.infra.persistence.handler.PersistenceHandler
import io.github.martinsjavacode.parkingmanagement.infra.persistence.repository.ParkingSpotRepository
import io.github.martinsjavacode.parkingmanagement.loggerFor
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class ParkingSpotRepositoryAdapter(
    private val parkingSpotRepository: ParkingSpotRepository,
    private val persistenceHandler: PersistenceHandler,
) : ParkingSpotRepositoryPort {
    private val logger = loggerFor<ParkingSpotRepositoryAdapter>()

    @Transactional(propagation = Propagation.SUPPORTS)
    override suspend fun findByCoordinates(
        latitude: Double,
        longitude: Double,
    ): ParkingSpot =
        persistenceHandler.handleOperation {
            val parkingSpotEntity = parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
            parkingSpotEntity.toDomain()
        }
}
