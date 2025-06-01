package io.github.martinsjavacode.parkingmanagement.domain.model.vehicle

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Represents the current status of a vehicle in the parking lot.
 *
 * This class contains information about the location, duration of stay,
 * and accumulated charges for a parked vehicle.
 *
 * @property licensePlate License plate of the vehicle
 * @property priceUntilNow Accumulated charges until now
 * @property entryTime Time the vehicle entered the parking lot
 * @property timeParked Time the vehicle was parked
 * @property latitude Latitude coordinate where the vehicle is parked
 * @property longitude Longitude coordinate where the vehicle is parked
 */
data class PlateStatus(
    val parkingEvent: ParkingEvent,
    val parking: Parking,
    val priceUntilNow: BigDecimal,
    val timeParked: LocalDateTime,
)
