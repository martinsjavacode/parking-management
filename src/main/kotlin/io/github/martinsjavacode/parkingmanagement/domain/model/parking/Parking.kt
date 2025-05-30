package io.github.martinsjavacode.parkingmanagement.domain.model.parking

import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalTime

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
