package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.spot.request

import io.swagger.v3.oas.annotations.media.Schema

data class SpotStatusRequest(
    @field:Schema(description = "Latitude of the parking spot", example = "40.7128", required = true)
    val lat: Double,
    @field:Schema(description = "Longitude of the parking spot", example = "-74.006", required = true)
    val lng: Double,
)
