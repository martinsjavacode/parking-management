package io.github.martinsjavacode.parkingmanagement.domain.model.parking

import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalTime

/**
 * Represents a parking lot in the system.
 *
 * This class contains all necessary information to define a parking lot,
 * including its sector, base price, maximum capacity, and operating hours.
 *
 * @property id Unique identifier of the parking lot, nullable for new instances
 * @property sector Name of the parking lot sector (e.g., "A", "B", "North", etc.)
 * @property basePrice Base price for the parking period
 * @property maxCapacity Maximum number of vehicles the parking lot can accommodate
 * @property openHour Opening hour of the parking lot
 * @property closeHour Closing hour of the parking lot
 * @property durationLimitMinutes Time limit in minutes for the base billing period
 * @property spots Flow of parking spots associated with this parking lot
 */
data class Parking(
    val id: Long?,
    val sector: String,
    val basePrice: BigDecimal,
    val maxCapacity: Int,
    val openHour: LocalTime,
    val closeHour: LocalTime,
    val durationLimitMinutes: Int,
    val spots: Flow<ParkingSpot>,
)
