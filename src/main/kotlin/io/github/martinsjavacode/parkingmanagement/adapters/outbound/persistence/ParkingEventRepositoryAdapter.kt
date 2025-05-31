package io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toDomain
import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toEntity
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_EVENT_LICENSE_PLATE_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_EVENT_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_EVENT_NOT_SAVED
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.LicensePlateNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingEventNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingEventSaveFailedException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingEventRepository
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

@Component
class ParkingEventRepositoryAdapter(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepository,
) : ParkingEventRepositoryPort {
    private val logger = loggerFor<ParkingEventRepositoryAdapter>()
    private val locale = LocaleContextHolder.getLocale()

    override suspend fun save(parkingEvent: ParkingEvent) {
        runCatching {
            val parkingEventEntity = parkingEvent.toEntity()
            parkingEventRepository.save(parkingEventEntity)
        }.onFailure {
            logger.error(
                "Failed to save parking event for this license plate. License Plate: {}, Trace ID: {}",
                parkingEvent.licensePlate,
                traceContext.traceId(),
            )
            throw ParkingEventSaveFailedException(
                PARKING_EVENT_NOT_SAVED.code(),
                messageSource.getMessage(
                    PARKING_EVENT_NOT_SAVED.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${PARKING_EVENT_NOT_SAVED.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.PERSISTENCE_REQUEST,
            )
        }
    }

    override suspend fun findAllByLicensePlate(licensePlate: String): Flow<ParkingEvent> =
        runCatching {
            parkingEventRepository.findByLicensePlate(licensePlate)
                ?.map { parkingEventEntity ->
                    parkingEventEntity.toDomain()
                }
        }.getOrNull() ?: throw LicensePlateNotFoundException(
            PARKING_EVENT_LICENSE_PLATE_NOT_FOUND.code(),
            messageSource.getMessage(
                PARKING_EVENT_LICENSE_PLATE_NOT_FOUND.messageKey(),
                null,
                locale,
            ),
            messageSource.getMessage(
                "${PARKING_EVENT_LICENSE_PLATE_NOT_FOUND.messageKey()}.friendly",
                null,
                locale,
            ),
            traceContext.traceId(),
            ExceptionType.PERSISTENCE_REQUEST,
        )

    override suspend fun findLastParkingEventByLicenseAndEventType(
        licensePlate: String,
        eventType: EventType,
    ): ParkingEvent =
        runCatching {
            parkingEventRepository.findByLicensePlateAndEventType(
                licensePlate = licensePlate,
                eventType = eventType,
            ).toDomain()
        }.getOrElse {
            logger.error("No active PARKED event found for the license plate: $licensePlate")
            throw ParkingEventNotFoundException(
                PARKING_EVENT_NOT_FOUND.code(),
                messageSource.getMessage(
                    PARKING_EVENT_NOT_FOUND.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${PARKING_EVENT_NOT_FOUND.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.PERSISTENCE_REQUEST,
            )
        }

    override suspend fun findMostRecentByCoordinates(
        latitude: Double,
        longitude: Double,
    ): ParkingEvent =
        runCatching {
            parkingEventRepository.findLastByLatitudeAndLongitude(latitude, longitude).toDomain()
        }.getOrElse {
            throw ParkingEventNotFoundException(
                PARKING_EVENT_NOT_FOUND.code(),
                messageSource.getMessage(
                    PARKING_EVENT_NOT_FOUND.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${PARKING_EVENT_NOT_FOUND.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.PERSISTENCE_REQUEST,
            )
        }
}
