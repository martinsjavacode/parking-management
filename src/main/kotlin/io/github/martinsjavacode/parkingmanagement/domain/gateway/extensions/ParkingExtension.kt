package io.github.martinsjavacode.parkingmanagement.domain.gateway.extensions

import io.github.martinsjavacode.parkingmanagement.domain.gateway.client.response.ParkingDataResponse
import io.github.martinsjavacode.parkingmanagement.domain.model.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.domain.rules.DateTimeRules
import io.github.martinsjavacode.parkingmanagement.infra.persistence.entity.ParkingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalTime

fun ParkingDataResponse.toDomain(parkingSpots: Flow<ParkingSpot>) = Parking(
    id = null,
    sector = this.sector,
    basePrice = this.basePrice,
    maxCapacity = this.maxCapacity,
    openHour = DateTimeRules.stringToLocalTime(this.openHour, "HH:mm"),
    closeHour = DateTimeRules.stringToLocalTime(this.closeHour, "HH:mm"),
    durationLimitMinutes = this.durationLimitMinutes,
    spots = parkingSpots
)

fun Parking.toEntity() = ParkingEntity(
    id = this.id,
    sector = this.sector,
    basePrice = this.basePrice,
    maxCapacity = this.maxCapacity,
    openHour = this.openHour,
    closeHour = this.closeHour,
    durationLimitMinutes = this.durationLimitMinutes,
)

fun ParkingEntity.toDomain() = Parking(
    id = this.id,
    sector = this.sector,
    basePrice = this.basePrice,
    maxCapacity = this.maxCapacity,
    openHour = this.openHour,
    closeHour = this.closeHour,
    durationLimitMinutes = this.durationLimitMinutes,
    spots = flowOf()
)
