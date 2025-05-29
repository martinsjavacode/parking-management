package io.github.martinsjavacode.parkingmanagement.infra.persistence

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_EVENT_LICENSE_PLATE_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.exception.LicensePlateNotFoundException
import io.github.martinsjavacode.parkingmanagement.domain.gateway.extensions.toDomain
import io.github.martinsjavacode.parkingmanagement.domain.gateway.extensions.toEntity
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.infra.persistence.repository.ParkingEventRepository
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class ParkingEventRepositoryAdapter(
    private val exceptionMessageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepository,
) : ParkingEventRepositoryPort {
    @Transactional
    override suspend fun save(parkingEvent: ParkingEvent) {
        val parkingEventEntity = parkingEvent.toEntity()
        parkingEventRepository.save(parkingEventEntity)
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    override suspend fun findByLicensePlate(licensePlate: String): ParkingEvent {
        val parkingEventEntity = parkingEventRepository.findByLicensePlate(licensePlate)
        return parkingEventEntity?.toDomain()
            ?: throw LicensePlateNotFoundException(
                PARKING_EVENT_LICENSE_PLATE_NOT_FOUND.code(),
                exceptionMessageSource.getMessage(
                    PARKING_EVENT_LICENSE_PLATE_NOT_FOUND.messageKey(),
                    null,
                    LocaleContextHolder.getLocale(),
                ),
                exceptionMessageSource.getMessage(
                    "${PARKING_EVENT_LICENSE_PLATE_NOT_FOUND.messageKey()}.friendly}",
                    null,
                    LocaleContextHolder.getLocale(),
                ),
                traceContext.traceId(),
                ExceptionType.EXTERNAL_REQUEST,
            )
    }
}
