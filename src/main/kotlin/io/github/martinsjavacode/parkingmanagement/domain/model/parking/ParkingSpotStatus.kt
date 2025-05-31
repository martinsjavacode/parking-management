package io.github.martinsjavacode.parkingmanagement.domain.model.parking

import java.math.BigDecimal
import java.time.LocalDateTime

data class ParkingSpotStatus(
    val parkingEvent: ParkingEvent,
    val occupied: Boolean,
    val priceUntilNow: BigDecimal,
    val timeParked: LocalDateTime,
)
