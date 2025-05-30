package io.github.martinsjavacode.parkingmanagement.service.webhook

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.WEBHOOK_ENTRY_EVENT_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.WEBHOOK_PARKED_EVENT_ALREADY_EXISTS
import io.github.martinsjavacode.parkingmanagement.domain.exception.EntryEventNotFoundException
import io.github.martinsjavacode.parkingmanagement.domain.exception.ParkedEventAlreadyExistsException
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import io.github.martinsjavacode.parkingmanagement.loggerFor
import io.github.martinsjavacode.parkingmanagement.service.CalculatePricingMultiplierHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

@Component
class ParkedWebhookHandler(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val calculatePricingMultiplierHandler: CalculatePricingMultiplierHandler,
) {
    private val logger = loggerFor<ParkedWebhookHandler>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)


    suspend fun handle(event: WebhookEvent) = supervisorScope {
        logger.info("New parked event received: {}", event.licensePlate)
        OperationalRules.checkCoordinates(latitude = event.lat, longitude = event.lng)

        val priceMultiplierDeferred = async {
            calculatePricingMultiplierHandler.execute(
                latitude = event.lat!!,
                longitude = event.lng!!
            )
        }
        val existingEventDeferred = async(dispatcherIO) { activeLicensePlate(event) }

        processExistingEvents(
            existingEvent = existingEventDeferred.await(),
            priceMultiplier = priceMultiplierDeferred.await(),
            event
        )
    }

    private suspend fun activeLicensePlate(event: WebhookEvent) =
        withContext(dispatcherIO) {
            parkingEventRepository.findActiveParkingEventByLicensePlate(
                licensePlate = event.licensePlate,
                latitude = event.lat ?: 0.0,
                longitude = event.lng ?: 0.0
            )
        }

    private suspend fun processExistingEvents(
        existingEvent: Flow<ParkingEvent>,
        priceMultiplier: Double,
        event: WebhookEvent
    ) {
        val parkedEvent = existingEvent.firstOrNull { it.eventType == EventType.PARKED }
        val entryEvent = existingEvent.firstOrNull { it.eventType == EventType.ENTRY }
        val locale = LocaleContextHolder.getLocale()
        if (parkedEvent != null) {
            throw ParkedEventAlreadyExistsException(
                WEBHOOK_PARKED_EVENT_ALREADY_EXISTS.code(),
                messageSource.getMessage(WEBHOOK_PARKED_EVENT_ALREADY_EXISTS.messageKey(), null, locale),
                messageSource.getMessage(
                    "${WEBHOOK_PARKED_EVENT_ALREADY_EXISTS.messageKey()}.friendly",
                    null,
                    locale
                ),
                traceContext.traceId(),
                ExceptionType.VALIDATION,
            )
        }

        if (entryEvent == null) {
            throw EntryEventNotFoundException(
                WEBHOOK_ENTRY_EVENT_NOT_FOUND.code(),
                messageSource.getMessage(WEBHOOK_ENTRY_EVENT_NOT_FOUND.messageKey(), null, locale),
                messageSource.getMessage(
                    "${WEBHOOK_ENTRY_EVENT_NOT_FOUND.messageKey()}.friendly",
                    null,
                    locale
                ),
                traceContext.traceId(),
                ExceptionType.VALIDATION,
            )
        }

        saveParkedEvent(entryEvent, priceMultiplier, event)
    }

    private suspend fun saveParkedEvent(
        entryEvent: ParkingEvent,
        priceMultiplier: Double,
        event: WebhookEvent
    ) {
        val parkedEvent = entryEvent.copy(
            latitude = event.lat!!,
            longitude = event.lng!!,
            eventType = EventType.PARKED,
            priceMultiplier = priceMultiplier,
        )
        withContext(dispatcherIO) {
            parkingEventRepository.save(parkedEvent)
        }
    }
}
