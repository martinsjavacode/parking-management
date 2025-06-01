package io.github.martinsjavacode.parkingmanagement.domain.model.parking

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Represents the current status of a parking spot.
 *
 * This class contains information about the occupancy of the spot,
 * including details about the parked vehicle, if any.
 *
 * @property occupied Indicates whether the spot is occupied
 * @property licensePlate License plate of the vehicle occupying the spot, if any
 * @property priceUntilNow Accumulated charges until now for the vehicle in the spot
 * @property entryTime Entry time of the vehicle occupying the spot
 * @property timeParked Time the vehicle was parked in the spot
 */
data class ParkingSpotStatus(
    val parkingEvent: ParkingEvent,
    val occupied: Boolean,
    val priceUntilNow: BigDecimal,
    val timeParked: LocalDateTime,
)
