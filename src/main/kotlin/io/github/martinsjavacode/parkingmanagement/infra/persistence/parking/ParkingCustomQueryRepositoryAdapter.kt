package io.github.martinsjavacode.parkingmanagement.infra.persistence.parking

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.exception.ParkingNotFoundException
import io.github.martinsjavacode.parkingmanagement.domain.extension.parking.toDomain
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingCapacityAndOccupancy
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingCustomQueryRepository
import io.github.martinsjavacode.parkingmanagement.loggerFor
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

@Component
class ParkingCustomQueryRepositoryAdapter(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingCustomQueryRepository: ParkingCustomQueryRepository,
) : ParkingCustomQueryRepositoryPort {
    companion object {
        private const val MAX_CAPACITY_DEFAULT = 1
        private const val SPOT_OCCUPANCY_DEFAULT = 0
    }

    private val logger = loggerFor<ParkingCustomQueryRepositoryAdapter>()
    private val locale = LocaleContextHolder.getLocale()

    override suspend fun findParkingCapacityAndOccupancy(
        latitude: Double,
        longitude: Double,
    ): ParkingCapacityAndOccupancy =
        // Search for the Max Capacity and Occupancy values; if they aren't found, use the default value for the occupancy percentage to result in 0.
        runCatching {
            parkingCustomQueryRepository.findParkingCapacityAndOccupancy(latitude, longitude)
        }.getOrNull() ?: ParkingCapacityAndOccupancy(
            MAX_CAPACITY_DEFAULT,
            SPOT_OCCUPANCY_DEFAULT,
        )

    override suspend fun findParkingByCoordinates(
        latitude: Double,
        longitude: Double,
    ): Parking =
        runCatching {
            logger.info("Searching for parking with latitude: $latitude and longitude: $longitude")
            parkingCustomQueryRepository.findParkingByLatitudeAndLongitude(latitude, longitude).toDomain()
        }.getOrElse {
            throw ParkingNotFoundException(
                PARKING_NOT_FOUND.code(),
                messageSource.getMessage(PARKING_NOT_FOUND.messageKey(), null, locale),
                messageSource.getMessage("${PARKING_NOT_FOUND.messageKey()}.friendly", null, locale),
                traceContext.traceId(),
                ExceptionType.VALIDATION,
            )
        }
}
