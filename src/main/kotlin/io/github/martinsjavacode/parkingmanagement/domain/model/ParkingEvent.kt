package io.github.martinsjavacode.parkingmanagement.domain.model

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import java.time.LocalDateTime

data class ParkingEvent(
    val id: Long? = null,
    val licensePlate: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val entryTime: LocalDateTime,
    var exitTime: LocalDateTime? = null,
    var eventType: EventType,
)
