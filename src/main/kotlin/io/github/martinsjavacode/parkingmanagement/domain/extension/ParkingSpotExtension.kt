package io.github.martinsjavacode.parkingmanagement.domain.extension

import io.github.martinsjavacode.parkingmanagement.domain.model.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.infra.persistence.entity.ParkingSpotEntity

fun ParkingSpot.toEntity() =
    ParkingSpotEntity(
        id = this.id,
        parkingId = this.parkingId ?: 0,
        latitude = this.latitude,
        longitude = this.longitude,
    )

fun ParkingSpotEntity.toDomain() =
    ParkingSpot(
        id = this.id,
        parkingId = this.parkingId,
        latitude = this.latitude,
        longitude = this.longitude,
    )
