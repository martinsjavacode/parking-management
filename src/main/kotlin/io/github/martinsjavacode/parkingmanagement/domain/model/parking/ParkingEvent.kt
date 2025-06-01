package io.github.martinsjavacode.parkingmanagement.domain.model.parking

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Represents an event related to parking a vehicle.
 *
 * This class records events such as vehicle entry, parking, and exit,
 * including information about location, timing, and payment amounts.
 *
 * @property id Unique identifier of the event, nullable for new instances
 * @property licensePlate License plate of the vehicle associated with the event
 * @property latitude Latitude coordinate where the event occurred
 * @property longitude Longitude coordinate where the event occurred
 * @property entryTime Time the vehicle entered the parking lot
 * @property exitTime Time the vehicle exited the parking lot, nullable for non-exit events
 * @property eventType Type of the event (ENTRY, PARKED, EXIT)
 * @property priceMultiplier Price multiplier applied to the event based on parking lot occupancy
 * @property amountPaid Amount paid for the parking, applicable only for exit events
 */
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
