package io.github.martinsjavacode.parkingmanagement.application.usecases.plate.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.plate.GetPlateStatusHandler
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.vehicle.PlateStatus
import io.github.martinsjavacode.parkingmanagement.domain.rules.DateTimeRules
import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class GetPlateStatusHandlerImpl(
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort,
) : GetPlateStatusHandler {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    override suspend fun handle(licensePlate: String): PlateStatus {
        val parkingEvent = fetchLastParkedEventByLicense(licensePlate)
        val parking = fetchParkingDetails(parkingEvent.latitude, parkingEvent.longitude)
        val now = LocalDateTime.now()

        val amountToPay = calculateAmountToPay(parkingEvent, parking, now)
        val elapsedTime = calculateElapsedTime(parkingEvent.entryTime, now)

        return buildPlateStatus(parkingEvent, parking, amountToPay, elapsedTime)
    }

    private suspend fun fetchLastParkedEventByLicense(licensePlate: String) =
        withContext(dispatcherIO) {
            parkingEventRepository.findLastParkingEventByLicenseAndEventType(licensePlate, EventType.PARKED)
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

    private fun buildPlateStatus(
        parkingEvent: ParkingEvent,
        parking: Parking,
        amountToPay: BigDecimal,
        elapsedTime: LocalDateTime
    ): PlateStatus {
        return PlateStatus(
            parkingEvent = parkingEvent,
            parking = parking,
            priceUntilNow = amountToPay,
            timeParked = elapsedTime,
        )
    }

}
