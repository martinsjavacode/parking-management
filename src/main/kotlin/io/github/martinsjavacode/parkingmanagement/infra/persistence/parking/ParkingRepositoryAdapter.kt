package io.github.martinsjavacode.parkingmanagement.infra.persistence.parking

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_NOT_SAVED
import io.github.martinsjavacode.parkingmanagement.domain.exception.ParkingNotFoundException
import io.github.martinsjavacode.parkingmanagement.domain.exception.ParkingSaveFailedException
import io.github.martinsjavacode.parkingmanagement.domain.extension.parking.toDomain
import io.github.martinsjavacode.parkingmanagement.domain.extension.parking.toEntity
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingRepository
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingSpotRepository
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

@Component
class ParkingRepositoryAdapter(
    private val parkingRepository: ParkingRepository,
    private val parkingSpotRepository: ParkingSpotRepository,
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
) : ParkingRepositoryPort {
    private val logger = loggerFor<ParkingRepositoryAdapter>()
    private val locale = LocaleContextHolder.getLocale()

    override suspend fun upsert(parking: Parking) {
        runCatching {
            val parkingEntity = parking.toEntity()
            parkingRepository.save(parkingEntity)
        }.onSuccess { parkingSaved ->
            if (parking.spots.count() > 0) {
                parking.spots
                    .map {
                        it.toEntity().copy(
                            id = null,
                            parkingId = parkingSaved.id!!,
                        )
                    }
                    .collect { parkingSpotEntity ->
                        parkingSpotRepository.save(parkingSpotEntity)
                    }
            }
        }.onFailure {
            logger.error(
                "Failed to save parking and its associated spots. Parking: ${parking.sector}, Trace ID: ${traceContext.traceId()}",
                it
            )

            throw ParkingSaveFailedException(
                PARKING_NOT_SAVED.code(),
                messageSource.getMessage(
                    PARKING_NOT_SAVED.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${PARKING_NOT_SAVED.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.PERSISTENCE_REQUEST,
            )
        }
    }

    override suspend fun findAll(): Flow<Parking> =
        runCatching {
            parkingRepository.findAll()
                .map { it.toDomain() }
        }.getOrElse {
            throw ParkingNotFoundException(
                PARKING_NOT_FOUND.code(),
                messageSource.getMessage(
                    PARKING_NOT_FOUND.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${PARKING_NOT_FOUND.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.PERSISTENCE_REQUEST,
            )
        }

    override suspend fun findBySectorName(sector: String): Parking =
        runCatching {
            parkingRepository.findBySector(sector)
        }.onFailure {
            throw ParkingNotFoundException(
                PARKING_NOT_FOUND.code(),
                messageSource.getMessage(
                    PARKING_NOT_FOUND.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${PARKING_NOT_FOUND.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.PERSISTENCE_REQUEST,
            )
        }.getOrThrow().toDomain()
}
