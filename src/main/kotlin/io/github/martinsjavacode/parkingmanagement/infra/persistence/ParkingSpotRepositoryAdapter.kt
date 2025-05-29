package io.github.martinsjavacode.parkingmanagement.infra.persistence

import io.github.martinsjavacode.parkingmanagement.domain.gateway.extensions.toDomain
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingSpotRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.infra.persistence.repository.ParkingSpotRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class ParkingSpotRepositoryAdapter(
    private val parkingSpotRepository: ParkingSpotRepository,
) : ParkingSpotRepositoryPort {
    @Transactional(propagation = Propagation.SUPPORTS)
    override suspend fun findByCoordinates(
        latitude: Double,
        longitude: Double,
    ): ParkingSpot {
        val parkingSpotEntity = parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
        return parkingSpotEntity.toDomain()
    }
}
