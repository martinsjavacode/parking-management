package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.vehicle.request

import com.fasterxml.jackson.annotation.JsonProperty

data class LicensePlateRequest(
    @JsonProperty("license_plate")
    val licensePlate: String,
)
