package io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetParkingByCoordinatesOrThrowHandler
import io.github.martinsjavacode.parkingmanagement.application.usecases.revenue.UpdateOrInitializeDailyRevenueHandler
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType.PARKED
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.WEBHOOK_CODE_EVENT_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.NoParkedEventFoundException
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Handler for processing vehicle exit events from parking lots.
 *
 * This class is responsible for validating and processing EXIT events,
 * calculating the charge, and updating daily revenue.
 *
 * @property messageSource Source for internationalized messages
 * @property traceContext Context for logging and tracing
 * @property parkingEventRepository Repository for parking events
 * @property getParkingByCoordinatesOrThrowHandler Handler to fetch parking by coordinates
 * @property updateDailyRevenueHandler Handler to update daily revenue
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Component
class ExitWebhookHandler(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val getParkingByCoordinatesOrThrowHandler: GetParkingByCoordinatesOrThrowHandler,
    private val updateDailyRevenueHandler: UpdateOrInitializeDailyRevenueHandler,
) {
    private val logger = loggerFor<ExitWebhookHandler>()
    private val locale = LocaleContextHolder.getLocale()

    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    /**
     * Processes a vehicle exit event.
     *
     * Validates the event data, fetches the corresponding parking event,
     * calculates the charge, and updates the daily revenue.
     *
     * @param event The webhook event to be processed
     * @throws IllegalArgumentException If the event type is invalid
     * @throws NoParkedEventFoundException If no parking event is found for the license plate
     */
    @Transactional
    suspend fun handle(event: WebhookEvent) {
        logger.info("New exit event received: {}", event.licensePlate)
        validateEventData(event)

        val parkingEvent = fetchParkingEvent(event.licensePlate)
        val parking =
            getParkingByCoordinatesOrThrowHandler.handle(
                latitude = parkingEvent.latitude,
                longitude = parkingEvent.longitude,
            )

        validateParkingData(parkingEvent, parking, event.licensePlate)

        val amountToPay =
            OperationalRules.calculateParkingFee(
                entryTime = parkingEvent.entryTime,
                exitTime = event.exitTime!!,
                basePrice = parking.basePrice,
                durationLimitMinutes = parking.durationLimitMinutes,
                priceMultiplier = parkingEvent.priceMultiplier,
            )

        supervisorScope {
            val updatedEventDeferred =
                async {
                    saveUpdatedParkingEvent(parkingEvent, amountToPay, event.exitTime)
                }

            val updateDailyRevenueDeferred =
                async {
                    updateDailyRevenueHandler.handle(
                        eventType = event.eventType,
                        latitude = parkingEvent.latitude,
                        longitude = parkingEvent.longitude,
                        amountPaid = amountToPay,
                    )
                }

            updatedEventDeferred.await()
            updateDailyRevenueDeferred.await()
        }
    }

    private fun validateEventData(event: WebhookEvent) {
        require(event.eventType == EventType.EXIT) { "Invalid event type: ${event.eventType}" }
        requireNotNull(event.exitTime) { "Exit time is required for EXIT event" }
    }

    private suspend fun fetchParkingEvent(licensePlate: String): ParkingEvent {
        val parkingEventsFound =
            withContext(dispatcherIO) {
                parkingEventRepository.findAllByLicensePlate(licensePlate)
            }

        return parkingEventsFound.firstOrNull { parkingEvent -> parkingEvent.eventType == PARKED }
            ?: throw NoParkedEventFoundException(
                WEBHOOK_CODE_EVENT_NOT_FOUND.code(),
                messageSource.getMessage(
                    WEBHOOK_CODE_EVENT_NOT_FOUND.messageKey(),
                    arrayOf(PARKED.name, licensePlate),
                    locale,
                ),
                messageSource.getMessage(
                    "${WEBHOOK_CODE_EVENT_NOT_FOUND.messageKey()}.friendly",
                    arrayOf(PARKED.name, licensePlate),
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.VALIDATION,
            )
    }

    private fun validateParkingData(
        parkingEvent: ParkingEvent,
        parking: Parking,
        licensePlate: String,
    ) {
        requireNotNull(parkingEvent) { "No parking event found for license plate: $licensePlate" }
        requireNotNull(
            parking,
        ) { "No parking found for coordinates: (${parkingEvent.latitude}, ${parkingEvent.longitude})" }
        OperationalRules.assertValidCoordinates(
            latitude = parkingEvent.latitude,
            longitude = parkingEvent.longitude,
        )
    }

    private suspend fun saveUpdatedParkingEvent(
        parkingEvent: ParkingEvent,
        amountToPay: BigDecimal,
        exitTime: LocalDateTime,
    ) {
        val updatedParkingEvent =
            parkingEvent.copy(
                amountPaid = amountToPay,
                exitTime = exitTime,
                eventType = EventType.EXIT,
            )

        withContext(dispatcherIO) {
            parkingEventRepository.save(updatedParkingEvent)
        }
    }
}
