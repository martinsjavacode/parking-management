package io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toDomain
import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toEntity
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_NOT_SAVED
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingSaveFailedException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingRepository
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingSpotRepository
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

/**
 * Adapter implementation for the parking repository port.
 *
 * This class implements the ParkingRepositoryPort interface,
 * providing concrete implementations for parking data operations.
 *
 * @property parkingRepository Repository for parking entities
 * @property parkingSpotRepository Repository for parking spot entities
 * @property messageSource Source for internationalized messages
 * @property traceContext Context for tracing requests
 */
@Component
class ParkingRepositoryAdapter(
    private val parkingRepository: ParkingRepository,
    private val parkingSpotRepository: ParkingSpotRepository,
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
) : ParkingRepositoryPort {
    private val logger = loggerFor<ParkingRepositoryAdapter>()
    private val locale = LocaleContextHolder.getLocale()

    /**
     * Inserts or updates a parking entity and its associated spots.
     *
     * @param parking The parking entity to be saved or updated
     * @throws ParkingSaveFailedException If the save operation fails
     */
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
                String.format(
                    "Failed to save parking and its associated spots. Parking: {}, Trace ID: {}",
                    parking.sector,
                    traceContext.traceId(),
                ),
                it,
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

    /**
     * Retrieves all parking entities.
     *
     * @return A flow of all parking entities
     * @throws ParkingNotFoundException If the retrieval operation fails
     */
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

    /**
     * Finds a parking entity by its sector name.
     *
     * @param sector The sector name to search for
     * @return The parking entity with the specified sector name
     * @throws ParkingNotFoundException If no parking with the given sector name is found
     */
    override suspend fun findBySectorName(sector: String): Parking =
        runCatching {
            parkingRepository.findBySector(sector).toDomain()
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
        }.getOrThrow()
}
