package io.github.martinsjavacode.parkingmanagement.infra.persistence

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_ALREADY_EXISTS
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_NOT_SAVED
import io.github.martinsjavacode.parkingmanagement.domain.exception.SaveParkingException
import io.github.martinsjavacode.parkingmanagement.domain.gateway.extensions.toEntity
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.Parking
import io.github.martinsjavacode.parkingmanagement.infra.client.ParkingClientAdapter
import io.github.martinsjavacode.parkingmanagement.infra.persistence.repository.ParkingRepository
import io.github.martinsjavacode.parkingmanagement.infra.persistence.repository.ParkingSpotRepository
import io.github.martinsjavacode.parkingmanagement.loggerFor
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
    private val traceContext: TraceContext
) : ParkingRepositoryPort {
    private val logger = loggerFor<ParkingClientAdapter>()

    @Transactional
    override suspend fun upsert(parking: Parking) {
        try {
            val parkingEntity = parking.toEntity()
            val parkingSaved = parkingRepository.save(parkingEntity)

            if (parkingSaved.id == null) {
                throw SaveParkingException(
                    PARKING_NOT_SAVED.code(),
                    messageSource.getMessage(
                        PARKING_NOT_SAVED.messageKey(),
                        null,
                        LocaleContextHolder.getLocale()
                    ),
                    messageSource.getMessage(
                        "${PARKING_NOT_SAVED.messageKey()}.friendly}",
                        null,
                        LocaleContextHolder.getLocale()
                    ),
                    traceContext.traceId(),
                    ExceptionType.EXTERNAL_REQUEST
                )
            }

            if (parking.spots.count() > 0) {
                parking.spots
                    .map { it.toEntity().copy(id = null, parkingId = parkingSaved.id!!) }
                    .collect { parkingSpotEntity -> parkingSpotRepository.save(parkingSpotEntity) }
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
            throw SaveParkingException(
                PARKING_ALREADY_EXISTS.code(),
                messageSource.getMessage(
                    PARKING_ALREADY_EXISTS.messageKey(),
                    null,
                    LocaleContextHolder.getLocale()
                ),
                messageSource.getMessage(
                    "${PARKING_ALREADY_EXISTS.messageKey()}.friendly}",
                    null,
                    LocaleContextHolder.getLocale()
                ),
                traceContext.traceId(),
                ExceptionType.EXTERNAL_REQUEST
            )
        }
    }

    override suspend fun findBySector(sector: String): Parking {
        TODO("Not yet implemented")
    }
}
