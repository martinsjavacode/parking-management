package io.github.martinsjavacode.parkingmanagement.service.webhook

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime

@Component
class ExitWebhookHandler(
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort,
) {

    private val logger = loggerFor<ExitWebhookHandler>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherDefault = Dispatchers.Default.limitedParallelism(50)

    suspend fun handle(event: WebhookEvent) {
        logger.info("New exit event received: {}", event.licensePlate)
        requireNotNull(event.exitTime) { "Exit time is required for EXIT event" }

        val parkingEvent = fetchParkingEvent(event.licensePlate)
        val parking = fetchParking(parkingEvent.latitude, parkingEvent.longitude)

        validateParkingData(parkingEvent, parking, event.licensePlate)

        val amountToPay = computeCharge(
            entryTime = parkingEvent.entryTime,
            exitTime = event.exitTime,
            basePrice = parking.basePrice,
            durationLimitMinutes = parking.durationLimitMinutes,
            priceMultiplier = parkingEvent.priceMultiplier
        )

        saveUpdatedParkingEvent(parkingEvent, amountToPay, event.exitTime)
        // ATUALIZAR REVENUE DO DIA
    }

    private suspend fun fetchParkingEvent(licensePlate: String) =
        withContext(dispatcherIO) {
            parkingEventRepository.findByLicensePlate(licensePlate)
        }

    private suspend fun fetchParking(latitude: Double, longitude: Double) =
        withContext(dispatcherIO) {
            parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
        }

    private fun validateParkingData(parkingEvent: ParkingEvent, parking: Parking, licensePlate: String) {
        requireNotNull(parkingEvent) { "No parking event found for license plate: $licensePlate" }
        requireNotNull(parking) {
            "No parking found for coordinates: (${parkingEvent.latitude}, ${parkingEvent.longitude})"
        }
    }

    private suspend fun saveUpdatedParkingEvent(
        parkingEvent: ParkingEvent,
        amountToPay: BigDecimal,
        exitTime: LocalDateTime
    ) {
        val updatedParkingEvent = parkingEvent.copy(
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
        priceMultiplier: Double
    ): BigDecimal = withContext(dispatcherDefault) {
        // Calculate the duration in minutes
        val durationMinutes = Duration.between(entryTime, exitTime).toMinutes()

        // Convert the duration into proportional "periods" based on the duration limit
        val period = BigDecimal(durationMinutes).divide(
            BigDecimal(durationLimitMinutes), // Convert duration limit to BigDecimal
            10, // Division precision
            RoundingMode.HALF_UP // Rounding mode
        )

        // Calculate the base amount proportional to the period
        val amountBase = period.multiply(basePrice)

        // Multiply by the price multiplier and return the final amount with 2 decimal places
        amountBase.multiply(BigDecimal.valueOf(priceMultiplier))
            .setScale(2, RoundingMode.HALF_UP)
    }

}
