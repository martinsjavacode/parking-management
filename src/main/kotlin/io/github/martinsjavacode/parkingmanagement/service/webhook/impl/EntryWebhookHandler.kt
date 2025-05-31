package io.github.martinsjavacode.parkingmanagement.service.webhook.impl

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType.ENTRY
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType.EXIT
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.WEBHOOK_ENTRY_LICENSE_PLATE_CONFLICT
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.WEBHOOK_ENTRY_NO_PARKING_OPEN
import io.github.martinsjavacode.parkingmanagement.domain.exception.LicensePlateConflictException
import io.github.martinsjavacode.parkingmanagement.domain.exception.NoParkingOpenException
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime

@Component
class EntryWebhookHandler(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val parkingRepository: ParkingRepositoryPort,
) {
    private val logger = loggerFor<EntryWebhookHandler>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    @Transactional
    suspend fun handle(event: WebhookEvent) {
        logger.info("New ENTRY event received: licensePlate={}, entryTime={}", event.licensePlate, event.entryTime)
        validateEventData(event)

        /*
            Check that the parking or sector is completely occupied at the moment and, if it is, stop the event process. Otherwise, process it.
            For it, we need:
            1. Fetch parking configurations by coordinates or by sector name
                1.1 If the parking is not found, stop the event process
                1.2 If the parking is found, continue the process
            2. Check if the parking spots is completely occupied
                2.1 If it is, stop the event process
                2.2. If it isn't, process it

            Without coordinates or sector name, it is not possible get to know which parking or gate is a hundred percent occupied in this event
         */

        // Checking if there is currently an open parking space.
        // This is not a perfect method of checking, as it only checks if there is a parking record during opening hours
        if (!isParkingCurrentlyOpen()) {
            logger.info("No parking is currently open. Event cannot be processed: licensePlate={}", event.licensePlate)
        } else {
            logger.info("Parking is open. Processing event for licensePlate={}", event.licensePlate)
            processEntryEvent(event)
        }
    }

    private suspend fun validateEventData(event: WebhookEvent) {
        require(event.eventType == ENTRY) { "Invalid event type: ${event.eventType}" }
        checkNotNull(event.entryTime) { "Entry time is required for ENTRY event" }
        checkForLicensePlateConflict(event.licensePlate)
    }

    private suspend fun processEntryEvent(event: WebhookEvent) {
        val entryEvent =
            ParkingEvent(
                licensePlate = event.licensePlate,
                entryTime = event.entryTime!!,
                eventType = event.eventType,
            )

        withContext(dispatcherIO) {
            logger.info("Saving new parking event for licensePlate={}", event.licensePlate)
            parkingEventRepository.save(entryEvent)
        }
    }

    private suspend fun checkForLicensePlateConflict(licensePlate: String) {
        val existingEvents = parkingEventRepository.findAllByLicensePlate(licensePlate)
            .firstOrNull { parkingEvent -> parkingEvent.eventType != EXIT }
        if (existingEvents != null) {
            val locale = LocaleContextHolder.getLocale()
            logger.warn(
                "Conflict detected: Active parking event already exists for licensePlate={}",
                licensePlate,
            )
            throw LicensePlateConflictException(
                WEBHOOK_ENTRY_LICENSE_PLATE_CONFLICT.code(),
                messageSource.getMessage(
                    WEBHOOK_ENTRY_LICENSE_PLATE_CONFLICT.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${WEBHOOK_ENTRY_LICENSE_PLATE_CONFLICT.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.PERSISTENCE_REQUEST,
            )
        }
    }

    private suspend fun isParkingCurrentlyOpen(): Boolean {
        val parking =
            withContext(dispatcherIO) {
                logger.debug("Fetching all parking configurations")
                parkingRepository.findAll()
            }

        if (parking.count() == 0) {
            val locale = LocaleContextHolder.getLocale()
            logger.warn("No parking configurations available in the system.")
            throw NoParkingOpenException(
                WEBHOOK_ENTRY_NO_PARKING_OPEN.code(),
                messageSource.getMessage(
                    WEBHOOK_ENTRY_NO_PARKING_OPEN.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${WEBHOOK_ENTRY_NO_PARKING_OPEN.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.VALIDATION,
            )
        }

        val now = LocalTime.now()
        val isOpen =
            parking.firstOrNull {
                it.openHour <= now && it.closeHour.isAfter(now)
            } != null

        logger.info("Parking status at {}: isOpen={}", now, isOpen)
        return isOpen
    }
}
