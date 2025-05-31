package io.github.martinsjavacode.parkingmanagement.infra.rest.v1.vehicle.request

import com.fasterxml.jackson.annotation.JsonProperty

data class LicensePlateRequest(
    @JsonProperty("license_plate")
    val licensePlate: String,
)
