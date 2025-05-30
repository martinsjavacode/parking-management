package io.github.martinsjavacode.parkingmanagement.domain.model.parking

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import java.math.BigDecimal
import java.time.LocalDateTime

data class ParkingEvent(
    val id: Long? = null,
    val licensePlate: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val entryTime: LocalDateTime,
    var exitTime: LocalDateTime? = null,
    var eventType: EventType,
    val priceMultiplier: Double = 1.0,
    val amountPaid: BigDecimal = BigDecimal.ZERO,
)
