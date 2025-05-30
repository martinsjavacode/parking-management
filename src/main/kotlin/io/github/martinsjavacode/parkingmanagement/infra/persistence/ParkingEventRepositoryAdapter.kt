package io.github.martinsjavacode.parkingmanagement.infra.persistence

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_EVENT_LICENSE_PLATE_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.exception.LicensePlateNotFoundException
import io.github.martinsjavacode.parkingmanagement.domain.extension.toDomain
import io.github.martinsjavacode.parkingmanagement.domain.extension.toEntity
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.infra.persistence.handler.PersistenceHandler
import io.github.martinsjavacode.parkingmanagement.infra.persistence.repository.ParkingEventRepository
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class ParkingEventRepositoryAdapter(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepository,
    private val persistenceHandler: PersistenceHandler
) : ParkingEventRepositoryPort {
    private val logger = loggerFor<ParkingEventRepositoryAdapter>()

    @Transactional
    override suspend fun save(parkingEvent: ParkingEvent) {
        persistenceHandler.handleOperation {
            val parkingEventEntity = parkingEvent.toEntity()
            parkingEventRepository.save(parkingEventEntity)
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    override suspend fun findByLicensePlate(licensePlate: String): ParkingEvent =
        persistenceHandler.handleOperation {
            val parkingEventEntity = parkingEventRepository.findByLicensePlate(licensePlate)
            parkingEventEntity?.toDomain()
                ?: throw LicensePlateNotFoundException(
                    PARKING_EVENT_LICENSE_PLATE_NOT_FOUND.code(),
                    messageSource.getMessage(
                        PARKING_EVENT_LICENSE_PLATE_NOT_FOUND.messageKey(),
                        null,
                        LocaleContextHolder.getLocale(),
                    ),
                    messageSource.getMessage(
                        "${PARKING_EVENT_LICENSE_PLATE_NOT_FOUND.messageKey()}.friendly",
                        null,
                        LocaleContextHolder.getLocale(),
                    ),
                    traceContext.traceId(),
                    ExceptionType.EXTERNAL_REQUEST,
                )
        }

    @Transactional(propagation = Propagation.SUPPORTS)
    override suspend fun findActiveParkingEventByLicensePlate(
        licensePlate: String,
        latitude: Double,
        longitude: Double,
    ): Flow<ParkingEvent> =
        persistenceHandler.handleOperation {
            val parkingEventEntity = parkingEventRepository.findParkingEventsByLicensePlateOrCoordinatesAndEventTypeNot(
                licensePlate = licensePlate,
                latitude = latitude,
                longitude = longitude,
                eventType = EventType.EXIT
            )

            parkingEventEntity.map { it.toDomain() }
        }
}
