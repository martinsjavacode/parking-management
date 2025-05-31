package io.github.martinsjavacode.parkingmanagement.adapters.extension.vehicle

import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.vehicle.response.PlateStatusResponse
import io.github.martinsjavacode.parkingmanagement.domain.model.vehicle.PlateStatus

fun PlateStatus.toResponse() =
    PlateStatusResponse(
        licensePlate = this.parkingEvent.licensePlate,
        priceUntilNow = this.priceUntilNow,
        entryTime = this.parkingEvent.entryTime,
        timeParked = this.timeParked,
        lat = this.parkingEvent.latitude,
        lng = this.parkingEvent.longitude,
    )
