package io.github.martinsjavacode.parkingmanagement.application.usecases.parking.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetParkingSpotStatusHandler
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpotStatus
import io.github.martinsjavacode.parkingmanagement.domain.rules.DateTimeRules
import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class GetParkingSpotStatusHandlerImpl(
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort,
) : GetParkingSpotStatusHandler {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    override suspend fun handle(latitude: Double, longitude: Double): ParkingSpotStatus {
        val parkingEvent = fetchMostRecentParkingEvent(latitude, longitude)
        val parking = fetchParkingDetails(parkingEvent.latitude, parkingEvent.longitude)
        val now = LocalDateTime.now()

        val amountToPay = calculateAmountToPay(parkingEvent, parking, now)
        val elapsedTime = calculateElapsedTime(parkingEvent.entryTime, now)

        return buildParkingSpotStatus(parkingEvent, amountToPay, elapsedTime)
    }

    private suspend fun fetchMostRecentParkingEvent(latitude: Double, longitude: Double) =
        withContext(dispatcherIO) {
            parkingEventRepository.findMostRecentByCoordinates(latitude, longitude)
        }

    private suspend fun fetchParkingDetails(latitude: Double, longitude: Double) =
        withContext(dispatcherIO) {
            parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
        }

    private fun calculateAmountToPay(
        parkingEvent: ParkingEvent,
        parking: Parking,
        now: LocalDateTime
    ): BigDecimal {
        return OperationalRules.calculateParkingFee(
            entryTime = parkingEvent.entryTime,
            exitTime = now,
            basePrice = parking.basePrice,
            durationLimitMinutes = parking.durationLimitMinutes,
            priceMultiplier = parkingEvent.priceMultiplier,
        )
    }

    private fun calculateElapsedTime(entryTime: LocalDateTime, now: LocalDateTime) =
        DateTimeRules.calculateElapsedTimeAsLocalTime(entryTime, now)

    private fun buildParkingSpotStatus(
        parkingEvent: ParkingEvent,
        amountToPay: BigDecimal,
        elapsedTime: LocalDateTime
    ): ParkingSpotStatus {
        val isOccupied = parkingEvent.eventType != EventType.EXIT
        return ParkingSpotStatus(
            parkingEvent = parkingEvent,
            occupied = isOccupied,
            priceUntilNow = if (isOccupied) amountToPay else parkingEvent.amountPaid,
            timeParked = if (isOccupied) elapsedTime else parkingEvent.exitTime!!,
        )
    }
}
