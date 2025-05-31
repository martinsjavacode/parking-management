package io.github.martinsjavacode.parkingmanagement.domain.model.vehicle

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import java.math.BigDecimal
import java.time.LocalDateTime

data class PlateStatus(
    val parkingEvent: ParkingEvent,
    val parking: Parking,
    val priceUntilNow: BigDecimal,
    val timeParked: LocalDateTime
)
