package io.github.martinsjavacode.parkingmanagement.service.webhook.impl

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType.PARKED
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.WEBHOOK_CODE_EVENT_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.exception.NoParkedEventFoundException
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import io.github.martinsjavacode.parkingmanagement.loggerFor
import io.github.martinsjavacode.parkingmanagement.service.parking.GetParkingByCoordinatesOrThrowHandler
import io.github.martinsjavacode.parkingmanagement.service.revenue.UpdateOrInitializeDailyRevenueHandler
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
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime

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

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherDefault = Dispatchers.Default.limitedParallelism(50)

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
            computeCharge(
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
        OperationalRules.checkCoordinates(
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

    private suspend fun computeCharge(
        entryTime: LocalDateTime,
        exitTime: LocalDateTime,
        basePrice: BigDecimal,
        durationLimitMinutes: Int,
        priceMultiplier: Double,
    ): BigDecimal =
        withContext(dispatcherDefault) {
            // Calculate the duration in minutes
            val durationMinutes = Duration.between(entryTime, exitTime).toMinutes()

            // Convert the duration into proportional "periods" based on the duration limit
            val period =
                BigDecimal(durationMinutes).divide(
                    // Convert duration limit to BigDecimal
                    BigDecimal(durationLimitMinutes),
                    // Division precision
                    10,
                    RoundingMode.HALF_UP,
                )

            // Calculate the base amount proportional to the period
            val amountBase = period.multiply(basePrice)

            // Multiply by the price multiplier and return the final amount with 2 decimal places
            amountBase.multiply(BigDecimal.valueOf(priceMultiplier))
                .setScale(2, RoundingMode.HALF_UP)
        }
}
