package io.github.martinsjavacode.parkingmanagement.infra.persistence

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_NOT_SAVED
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.UNEXPECTED_DATABASE_ERROR
import io.github.martinsjavacode.parkingmanagement.domain.exception.ParkingSaveFailedException
import io.github.martinsjavacode.parkingmanagement.domain.exception.UnexpectedDatabaseException
import io.github.martinsjavacode.parkingmanagement.domain.extension.toDomain
import io.github.martinsjavacode.parkingmanagement.domain.extension.toEntity
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.Parking
import io.github.martinsjavacode.parkingmanagement.infra.persistence.handler.PersistenceHandler
import io.github.martinsjavacode.parkingmanagement.infra.persistence.repository.ParkingRepository
import io.github.martinsjavacode.parkingmanagement.infra.persistence.repository.ParkingSpotRepository
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ParkingRepositoryAdapter(
    private val parkingRepository: ParkingRepository,
    private val parkingSpotRepository: ParkingSpotRepository,
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val persistenceHandler: PersistenceHandler
) : ParkingRepositoryPort {
    private val logger = loggerFor<ParkingRepositoryAdapter>()

    @Transactional
    override suspend fun upsert(parking: Parking) {
        runCatching {
            val parkingEntity = parking.toEntity()
            val parkingSaved = parkingRepository.save(parkingEntity)

            if (parkingSaved.id == null) {
                throw ParkingSaveFailedException(
                    PARKING_NOT_SAVED.code(),
                    messageSource.getMessage(
                        PARKING_NOT_SAVED.messageKey(),
                        null,
                        LocaleContextHolder.getLocale(),
                    ),
                    messageSource.getMessage(
                        "${PARKING_NOT_SAVED.messageKey()}.friendly",
                        null,
                        LocaleContextHolder.getLocale(),
                    ),
                    traceContext.traceId(),
                    ExceptionType.EXTERNAL_REQUEST,
                )
            }

            if (parking.spots.count() > 0) {
                parking.spots
                    .map {
                        it.toEntity().copy(
                            id = null,
                            parkingId = parkingSaved.id!!,
                        )
                    }
                    .collect { parkingSpotEntity -> parkingSpotRepository.save(parkingSpotEntity) }
            }
        }.onFailure {
            logger.error(it.message, it)
            throw UnexpectedDatabaseException(
                UNEXPECTED_DATABASE_ERROR.code(),
                messageSource.getMessage(
                    UNEXPECTED_DATABASE_ERROR.messageKey(),
                    null,
                    LocaleContextHolder.getLocale(),
                ),
                messageSource.getMessage(
                    "${UNEXPECTED_DATABASE_ERROR.messageKey()}.friendly",
                    null,
                    LocaleContextHolder.getLocale(),
                ),
                traceContext.traceId(),
                ExceptionType.EXTERNAL_REQUEST,
            )
        }
    }

    override suspend fun findAll(): Flow<Parking> =
        persistenceHandler.handleOperation {
            parkingRepository.findAll()
                .map { it.toDomain() }
        }
}
