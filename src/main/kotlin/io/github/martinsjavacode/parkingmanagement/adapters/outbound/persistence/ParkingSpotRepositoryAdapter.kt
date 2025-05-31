package io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toDomain
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_SPOT_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingSpotRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingSpotNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingSpotRepository
import io.github.martinsjavacode.parkingmanagement.loggerFor
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

@Component
class ParkingSpotRepositoryAdapter(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingSpotRepository: ParkingSpotRepository,
) : ParkingSpotRepositoryPort {
    private val logger = loggerFor<ParkingSpotRepositoryAdapter>()
    private val locale = LocaleContextHolder.getLocale()

    override suspend fun findByCoordinates(
        latitude: Double,
        longitude: Double,
    ): ParkingSpot =
        runCatching {
            val parkingSpotEntity = parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
            parkingSpotEntity.toDomain()
        }.onFailure {
            logger.error("Error finding parking spot at coordinates: ($latitude, $longitude)", it)
            throw ParkingSpotNotFoundException(
                PARKING_SPOT_NOT_FOUND.code(),
                messageSource.getMessage(
                    PARKING_SPOT_NOT_FOUND.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${PARKING_SPOT_NOT_FOUND.messageKey()}.friendly",
                    null,
                    LocaleContextHolder.getLocale(),
                ),
                traceContext.traceId(),
                ExceptionType.PERSISTENCE_REQUEST,
            )
        }.getOrThrow()
}
