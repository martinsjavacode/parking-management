package io.github.martinsjavacode.parkingmanagement.service.parking.impl

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.exception.ParkingNotFoundException
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.service.parking.GetParkingByCoordinatesOrThrowHandler
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service

@Service
class GetParkingByCoordinatesOrThrowHandlerImpl(
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort,
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
) : GetParkingByCoordinatesOrThrowHandler {
    private val locale = LocaleContextHolder.getLocale()

    override suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): Parking =
        parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
            ?: throw ParkingNotFoundException(
                PARKING_NOT_FOUND.code(),
                messageSource.getMessage(PARKING_NOT_FOUND.messageKey(), null, locale),
                messageSource.getMessage("${PARKING_NOT_FOUND.messageKey()}.friendly", null, locale),
                traceContext.traceId(),
                ExceptionType.VALIDATION,
            )
}
