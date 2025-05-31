package io.github.martinsjavacode.parkingmanagement.domain.extension.vehicle

import io.github.martinsjavacode.parkingmanagement.domain.model.vehicle.PlateStatus
import io.github.martinsjavacode.parkingmanagement.infra.rest.v1.vehicle.response.PlateStatusResponse

fun PlateStatus.toResponse() = PlateStatusResponse(
    licensePlate = this.parkingEvent.licensePlate,
    priceUntilNow = this.priceUntilNow,
    entryTime = this.parkingEvent.entryTime,
    timeParked = this.timeParked,
    lat = this.parkingEvent.latitude,
    lng = this.parkingEvent.longitude
)
