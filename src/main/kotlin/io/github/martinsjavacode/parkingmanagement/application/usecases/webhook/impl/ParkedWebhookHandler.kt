package io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.CalculatePricingMultiplierHandler
import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetMostRecentParkingEvent
import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetParkingByCoordinatesOrThrowHandler
import io.github.martinsjavacode.parkingmanagement.application.usecases.revenue.UpdateOrInitializeDailyRevenueHandler
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType.ENTRY
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType.PARKED
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_EVENT_DUPLICATE_EVENT
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.PARKING_SPOT_OCCUPIED
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.WEBHOOK_EVENT_ACTIVE_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.gateway.redis.DistributedLockPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.DuplicateEventException
import io.github.martinsjavacode.parkingmanagement.infra.exception.EntryEventNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingSpotOccupiedException
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Handler for processing vehicle parking events.
 *
 * This class is responsible for validating and processing PARKED events,
 * applying dynamic pricing multipliers based on sector occupancy.
 *
 * @property messageSource Source for internationalized messages
 * @property traceContext Context for logging and tracing
 * @property parkingEventRepository Port for repository of parking events
 * @property getParkingByCoordinatesOrThrowHandler Handler to fetch parking by coordinates
 * @property calculatePricingMultiplierHandler Handler to calculate pricing multiplier
 * @property initializeDailyRevenueHandler Handler to initialize daily revenue
 * @property distributedLock Port for distributed locking to handle concurrency
 */
@Component
class ParkedWebhookHandler(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val getParkingByCoordinatesOrThrowHandler: GetParkingByCoordinatesOrThrowHandler,
    private val calculatePricingMultiplierHandler: CalculatePricingMultiplierHandler,
    private val initializeDailyRevenueHandler: UpdateOrInitializeDailyRevenueHandler,
    private val getMostRecentParkingEvent: GetMostRecentParkingEvent,
    private val distributedLock: DistributedLockPort,
) {
    private val logger = loggerFor<ParkedWebhookHandler>()
    private val locale = LocaleContextHolder.getLocale()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    /**
     * Processes a vehicle parking event.
     *
     * Validates the event data, fetches the corresponding entry event,
     * calculates the dynamic pricing multiplier, and logs the parking event.
     * Also initializes the daily revenue record if needed.
     *
     * @param event The webhook event to be processed
     * @throws IllegalArgumentException If the event type is invalid
     * @throws EntryEventNotFoundException If no entry event is found for the license plate
     * @throws ParkingSpotOccupiedException If the spot is already occupied or locked
     */
    @Transactional
    suspend fun handle(event: WebhookEvent) {
        logger.info("New parked event received: {}", event.licensePlate)
        val latitude = event.lat!!
        val longitude = event.lng!!

        validateEventData(latitude, longitude, event.eventType)

        checkIfSpotExists(latitude, longitude)

        // Check for idempotency first - if we've already processed this exact event, skip processing
        if (!distributedLock.checkAndMarkIdempotency(
                latitude = latitude,
                longitude = longitude,
                eventId = event.id ?: (event.licensePlate + System.currentTimeMillis()),
            )
        ) {
            logger.info(
                "Duplicate event detected for coordinates: ({}, {}). Skipping processing.",
                latitude,
                longitude,
            )
            throw DuplicateEventException(
                PARKING_EVENT_DUPLICATE_EVENT.code(),
                messageSource.getMessage(
                    PARKING_EVENT_DUPLICATE_EVENT.messageKey(),
                    arrayOf(latitude, longitude),
                    locale,
                ),
                messageSource.getMessage(
                    "${PARKING_EVENT_DUPLICATE_EVENT.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.VALIDATION,
            )
        }

        // Try to acquire a lock on this parking spot
        val lockAcquired =
            distributedLock.acquireLock(
                latitude = latitude,
                longitude = longitude,
                licensePlate = event.licensePlate,
            )

        if (!lockAcquired) {
            logger.warn(
                "Could not acquire lock for parking spot at coordinates: ({}, {}). Another operation is in progress.",
                latitude,
                longitude,
            )
            throw ParkingSpotOccupiedException(
                PARKING_SPOT_OCCUPIED.code(),
                messageSource.getMessage(
                    PARKING_SPOT_OCCUPIED.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${PARKING_SPOT_OCCUPIED.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.VALIDATION,
            )
        }

        try {
            // Double-check if spot is occupied after acquiring the lock
            checkIfSpotOccupied(latitude, longitude)

            supervisorScope {
                val priceMultiplierDeferred =
                    async {
                        calculatePricingMultiplierHandler.handle(
                            latitude = latitude,
                            longitude = longitude,
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

                // If the day does not yet have a record in the revenue table,
                // it creates a record for the day with an initial amount of 0.
                initializeDailyRevenueHandler.handle(
                    eventType = event.eventType,
                    latitude = latitude,
                    longitude = longitude,
                )
            }
        } finally {
            // Always release the lock when done
            distributedLock.releaseLock(latitude, longitude, event.licensePlate)
        }
    }

    private suspend fun validateEventData(
        latitude: Double,
        longitude: Double,
        eventType: EventType,
    ) {
        require(eventType == PARKED) { "Invalid event type: ${eventType.name}" }
        OperationalRules.assertValidCoordinates(latitude, longitude)
    }

    private suspend fun checkIfSpotOccupied(
        latitude: Double,
        longitude: Double,
    ) {
        val parkingEvent =
            runCatching {
                getMostRecentParkingEvent.handle(latitude, longitude)
            }.getOrNull()

        if (parkingEvent != null && parkingEvent.eventType == PARKED) {
            throw ParkingSpotOccupiedException(
                PARKING_SPOT_OCCUPIED.code(),
                messageSource.getMessage(
                    PARKING_SPOT_OCCUPIED.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${PARKING_SPOT_OCCUPIED.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.VALIDATION,
            )
        }
    }

    private suspend fun checkIfSpotExists(
        latitude: Double,
        longitude: Double,
    ) {
        // If not exist spot for this parking, a ParkingNotFoundException will be thrown
        getParkingByCoordinatesOrThrowHandler.handle(latitude, longitude)
    }

    private suspend fun processExistingEvents(
        existingEvent: Flow<ParkingEvent>,
        priceMultiplier: Double,
        event: WebhookEvent,
    ) {
        val entryEvent =
            existingEvent.firstOrNull { parkingEvent ->
                parkingEvent.eventType == ENTRY
            } ?: throw EntryEventNotFoundException(
                WEBHOOK_EVENT_ACTIVE_NOT_FOUND.code(),
                messageSource.getMessage(
                    WEBHOOK_EVENT_ACTIVE_NOT_FOUND.messageKey(),
                    arrayOf(ENTRY.name, event.licensePlate),
                    locale,
                ),
                messageSource.getMessage(
                    "${WEBHOOK_EVENT_ACTIVE_NOT_FOUND.messageKey()}.friendly",
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
