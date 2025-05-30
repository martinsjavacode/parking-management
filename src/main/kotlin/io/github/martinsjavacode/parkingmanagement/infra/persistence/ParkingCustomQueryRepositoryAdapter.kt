package io.github.martinsjavacode.parkingmanagement.infra.persistence

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.exception.ParkingNotFoundException
import io.github.martinsjavacode.parkingmanagement.domain.extension.toDomain
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingCapacityAndOccupancy
import io.github.martinsjavacode.parkingmanagement.infra.persistence.handler.PersistenceHandler
import io.github.martinsjavacode.parkingmanagement.infra.persistence.repository.ParkingCustomQueryRepository
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class ParkingCustomQueryRepositoryAdapter(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingCustomQueryRepository: ParkingCustomQueryRepository,
    private val persistenceHandler: PersistenceHandler,
) : ParkingCustomQueryRepositoryPort {
    companion object {
        private const val MAX_CAPACITY_DEFAULT = 1
        private const val SPOT_OCCUPANCY_DEFAULT = 0
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    override suspend fun findParkingCapacityAndOccupancy(
        latitude: Double,
        longitude: Double,
    ): ParkingCapacityAndOccupancy =
        persistenceHandler.handleOperation {
            // Search for the Max Capacity and Occupancy values; if they aren't found, use the default value for the occupancy percentage to result in 0.
            parkingCustomQueryRepository.findParkingCapacityAndOccupancy(latitude, longitude)
                ?: ParkingCapacityAndOccupancy(MAX_CAPACITY_DEFAULT, SPOT_OCCUPANCY_DEFAULT)
        }

    @Transactional(propagation = Propagation.SUPPORTS)
    override suspend fun findParkingByCoordinates(
        latitude: Double,
        longitude: Double,
    ): Parking =
        persistenceHandler.handleOperation {
            parkingCustomQueryRepository.findParkingByLatitudeAndLongitude(latitude, longitude)?.toDomain()
                ?: throw ParkingNotFoundException(
                    PARKING_NOT_FOUND.code(),
                    messageSource.getMessage(
                        PARKING_NOT_FOUND.messageKey(),
                        null,
                        LocaleContextHolder.getLocale(),
                    ),
                    messageSource.getMessage(
                        "${PARKING_NOT_FOUND.messageKey()}.friendly",
                        null,
                        LocaleContextHolder.getLocale(),
                    ),
                    traceContext.traceId(),
                    ExceptionType.EXTERNAL_REQUEST,
                )
        }
}
