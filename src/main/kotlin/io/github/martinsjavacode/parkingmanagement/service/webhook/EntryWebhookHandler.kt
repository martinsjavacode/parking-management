package io.github.martinsjavacode.parkingmanagement.service.webhook

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.WEBHOOK_ENTRY_LICENSE_PLATE_CONFLICT
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.WEBHOOK_ENTRY_NO_PARKING_OPEN
import io.github.martinsjavacode.parkingmanagement.domain.exception.LicensePlateConflictException
import io.github.martinsjavacode.parkingmanagement.domain.exception.NoParkingOpenException
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import java.time.LocalTime

@Component
class EntryWebhookHandler(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val parkingRepository: ParkingRepositoryPort
) {
    private val logger = loggerFor<EntryWebhookHandler>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    suspend fun handle(event: WebhookEvent) {
        checkNotNull(event.entryTime) { "Entry time is required for ENTRY event" }
        logger.info("New ENTRY event received: licensePlate={}, entryTime={}", event.licensePlate, event.entryTime)

        if (!isParkingCurrentlyOpen()) {
            logger.info("No parking is currently open. Event cannot be processed: licensePlate={}", event.licensePlate)
        } else {
            logger.info("Parking is open. Processing event for licensePlate={}", event.licensePlate)
            processEntryEvent(event)
        }
    }

    private suspend fun processEntryEvent(event: WebhookEvent) {
        val entryEvent = ParkingEvent(
            licensePlate = event.licensePlate,
            entryTime = event.entryTime,
            eventType = event.eventType,
        )

        val existingEvents = fetchActiveLicensePlateEvents(event)
        if (existingEvents.count() > 0) {
            val locale = LocaleContextHolder.getLocale()
            logger.warn(
                "Conflict detected: Active parking event already exists for licensePlate={}",
                event.licensePlate
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
                ExceptionType.EXTERNAL_REQUEST,
            )
        }

        withContext(dispatcherIO) {
            logger.info("Saving new parking event for licensePlate={}", event.licensePlate)
            parkingEventRepository.save(entryEvent)
        }
    }

    private suspend fun fetchActiveLicensePlateEvents(event: WebhookEvent) =
        withContext(dispatcherIO) {
            logger.debug("Checking active parking events for licensePlate={}", event.licensePlate)
            parkingEventRepository.findActiveParkingEventByLicensePlate(
                licensePlate = event.licensePlate,
                latitude = event.lat ?: 0.0,
                longitude = event.lng ?: 0.0
            )
        }

    private suspend fun isParkingCurrentlyOpen(): Boolean {
        val parking = withContext(dispatcherIO) {
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
                ExceptionType.EXTERNAL_REQUEST,
            )
        }

        val now = LocalTime.now()
        val isOpen = parking.firstOrNull {
            it.openHour <= now && it.closeHour.isAfter(now)
        } != null

        logger.info("Parking status at {}: isOpen={}", now, isOpen)
        return isOpen
    }
}
