package io.github.martinsjavacode.parkingmanagement.service.plate.impl

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.vehicle.PlateStatus
import io.github.martinsjavacode.parkingmanagement.domain.rules.DateTimeRules
import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import io.github.martinsjavacode.parkingmanagement.service.plate.GetPlateStatusHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class GetPlateStatusHandlerImpl(
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort
) : GetPlateStatusHandler {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    override suspend fun handle(licensePlate: String): PlateStatus {
        val parkingEvent = withContext(dispatcherIO) {
            parkingEventRepository.findLastParkingEventByLicenseAndEventType(licensePlate, EventType.PARKED)
        }

        val parking = withContext(dispatcherIO) {
            parkingCustomQueryRepository.findParkingByCoordinates(
                latitude = parkingEvent.latitude,
                longitude = parkingEvent.longitude
            )
        }

        val now = LocalDateTime.now()

        val amountToPay =
            OperationalRules.calculateParkingFee(
                entryTime = parkingEvent.entryTime,
                exitTime = now,
                basePrice = parking.basePrice,
                durationLimitMinutes = parking.durationLimitMinutes,
                priceMultiplier = parkingEvent.priceMultiplier,
            )

        return PlateStatus(
            parkingEvent = parkingEvent,
            parking = parking,
            priceUntilNow = amountToPay,
            timeParked = DateTimeRules.calculateElapsedTimeAsLocalTime(parkingEvent.entryTime, now)
        )
    }
}
