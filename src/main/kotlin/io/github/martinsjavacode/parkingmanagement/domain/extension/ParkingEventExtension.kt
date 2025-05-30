package io.github.martinsjavacode.parkingmanagement.domain.extension

import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.infra.persistence.entity.ParkingEventEntity

fun ParkingEvent.toEntity() =
    ParkingEventEntity(
        id = this.id,
        licensePlate = this.licensePlate,
        entryTime = this.entryTime,
        eventType = this.eventType,
        latitude = this.latitude,
        longitude = this.longitude,
        exitTime = this.exitTime,
        priceMultiplier = this.priceMultiplier,
        amountPaid = this.amountPaid
    )

fun ParkingEventEntity.toDomain() =
    ParkingEvent(
        id = this.id,
        licensePlate = this.licensePlate,
        entryTime = this.entryTime,
        eventType = this.eventType,
        latitude = this.latitude,
        longitude = this.longitude,
        exitTime = this.exitTime,
        priceMultiplier = this.priceMultiplier,
        amountPaid = this.amountPaid
    )
