package io.github.martinsjavacode.parkingmanagement.service

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_EVENT_TYPE_INVALID
import io.github.martinsjavacode.parkingmanagement.domain.exception.InvalidParkingEventTypeException
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingSpotRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service

@Service
class WebhookService(
    private val exceptionMessageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val parkingSpotRepository: ParkingSpotRepositoryPort,
) {
    private val logger = loggerFor<WebhookService>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    suspend fun processEvent(event: WebhookEvent) {
        when (event.eventType) {
            EventType.ENTRY -> handleEntry(event)
            EventType.PARKED -> handleParked(event)
            EventType.EXIT -> handleExit(event)
        }
    }

    private suspend fun handleEntry(event: WebhookEvent) {
        checkNotNull(event.entryTime) { "Entry time is required for ENTRY event" }
        logger.info("New entry event received: {}", event.licensePlate)

        /*
            Here should be made the rules of parking capacity and check if it is open.
            But for this, we need that be sent the field "sector" in payload
         */

        val entryEvent =
            ParkingEvent(
                licensePlate = event.licensePlate,
                entryTime = event.entryTime,
                eventType = event.eventType,
            )

        withContext(dispatcherIO) {
            parkingEventRepository.save(entryEvent)
        }
    }

    private suspend fun handleParked(event: WebhookEvent) {
        checkNotNull(event.lat) { "Latitude is required for PARKED event" }
        checkNotNull(event.lng) { "Longitude is required for PARKED event" }

        logger.info("New parked event received: {}", event.licensePlate)

        val existingEvent =
            withContext(dispatcherIO) {
                parkingEventRepository.findByLicensePlate(event.licensePlate)
            }

        if (existingEvent.eventType != EventType.ENTRY) {
            throw InvalidParkingEventTypeException(
                PARKING_EVENT_TYPE_INVALID.code(),
                exceptionMessageSource.getMessage(
                    PARKING_EVENT_TYPE_INVALID.messageKey(),
                    null,
                    LocaleContextHolder.getLocale(),
                ),
                exceptionMessageSource.getMessage(
                    "${PARKING_EVENT_TYPE_INVALID.messageKey()}.friendly}",
                    null,
                    LocaleContextHolder.getLocale(),
                ),
                traceContext.traceId(),
                ExceptionType.EXTERNAL_REQUEST,
            )
        }

        val parkedEvent =
            existingEvent.copy(
                latitude = event.lat,
                longitude = event.lng,
                eventType = EventType.PARKED,
            )

        withContext(dispatcherIO) {
            parkingEventRepository.save(parkedEvent)
        }
    }

    private suspend fun handleExit(event: WebhookEvent) {
        logger.info("New exit event received: {}", event.licensePlate)

        withContext(dispatcherIO) {
            val parkingEvent =
                parkingEventRepository.findByLicensePlate(event.licensePlate)
                    .copy(exitTime = event.exitTime, eventType = EventType.EXIT)

            parkingEventRepository.save(parkingEvent)
        }
    }
}
