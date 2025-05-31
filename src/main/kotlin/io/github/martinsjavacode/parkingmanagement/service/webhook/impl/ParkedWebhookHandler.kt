package io.github.martinsjavacode.parkingmanagement.service.webhook.impl

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType.ENTRY
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType.PARKED
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.WEBHOOK_CODE_EVENT_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.WEBHOOK_PARKED_EVENT_ALREADY_EXISTS
import io.github.martinsjavacode.parkingmanagement.domain.exception.EntryEventNotFoundException
import io.github.martinsjavacode.parkingmanagement.domain.exception.ParkedEventAlreadyExistsException
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import io.github.martinsjavacode.parkingmanagement.loggerFor
import io.github.martinsjavacode.parkingmanagement.service.parking.CalculatePricingMultiplierHandler
import io.github.martinsjavacode.parkingmanagement.service.parking.FetchActiveLicensePlateEventsHandler
import io.github.martinsjavacode.parkingmanagement.service.parking.GetParkingByCoordinatesOrThrowHandler
import io.github.martinsjavacode.parkingmanagement.service.revenue.UpdateOrInitializeDailyRevenueHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ParkedWebhookHandler(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val getParkingByCoordinatesOrThrowHandler: GetParkingByCoordinatesOrThrowHandler,
    private val calculatePricingMultiplierHandler: CalculatePricingMultiplierHandler,
    private val fetchActiveLicensePlateEventsHandler: FetchActiveLicensePlateEventsHandler,
    private val initializeDailyRevenueHandler: UpdateOrInitializeDailyRevenueHandler,
) {
    private val logger = loggerFor<ParkedWebhookHandler>()
    private val locale = LocaleContextHolder.getLocale()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    @Transactional
    suspend fun handle(event: WebhookEvent) {
        logger.info("New parked event received: {}", event.licensePlate)
        validateEventData(event)

        supervisorScope {
            val priceMultiplierDeferred =
                async {
                    calculatePricingMultiplierHandler.handle(
                        latitude = event.lat!!,
                        longitude = event.lng!!,
                    )
                }
            val existingEventDeferred =
                async(dispatcherIO) {
                    parkingEventRepository.findAllByLicensePlate(event.licensePlate)
                }

            processExistingEvents(
                existingEvent = existingEventDeferred.await(),
                priceMultiplier = priceMultiplierDeferred.await(),
                event,
            )

            // If the day does not yet have a record in the revenue table, it creates a record for the day with an initial amount of 0.
            initializeDailyRevenueHandler.handle(
                eventType = event.eventType,
                latitude = event.lat!!,
                longitude = event.lng!!,
            )
        }
    }

    private suspend fun validateEventData(event: WebhookEvent) {
        require(event.eventType == PARKED) { "Invalid event type: ${event.eventType}" }
        OperationalRules.checkCoordinates(latitude = event.lat, longitude = event.lng)
        getParkingByCoordinatesOrThrowHandler.handle(latitude = event.lat!!, longitude = event.lng!!)
    }

    private suspend fun processExistingEvents(
        existingEvent: Flow<ParkingEvent>,
        priceMultiplier: Double,
        event: WebhookEvent,
    ) {
        val entryEvent = existingEvent.firstOrNull {
            parkingEvent -> parkingEvent.eventType == ENTRY
        } ?: throw EntryEventNotFoundException(
            WEBHOOK_CODE_EVENT_NOT_FOUND.code(),
            messageSource.getMessage(
                WEBHOOK_CODE_EVENT_NOT_FOUND.messageKey(),
                arrayOf(ENTRY.name, event.licensePlate),
                locale,
            ),
            messageSource.getMessage(
                "${WEBHOOK_CODE_EVENT_NOT_FOUND.messageKey()}.friendly",
                arrayOf(ENTRY.name, event.licensePlate),
                locale,
            ),
            traceContext.traceId(),
            ExceptionType.VALIDATION,
        )

        saveParkedEvent(entryEvent, priceMultiplier, event)
    }

    private suspend fun saveParkedEvent(
        entryEvent: ParkingEvent,
        priceMultiplier: Double,
        event: WebhookEvent,
    ) {
        val parkedEvent =
            entryEvent.copy(
                latitude = event.lat!!,
                longitude = event.lng!!,
                eventType = PARKED,
                priceMultiplier = priceMultiplier,
            )
        withContext(dispatcherIO) {
            parkingEventRepository.save(parkedEvent)
        }
    }
}
