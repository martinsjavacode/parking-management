package io.github.martinsjavacode.parkingmanagement.adapters.outbound.client.response

import com.fasterxml.jackson.annotation.JsonAutoDetect

@JsonAutoDetect
data class ParkingSpotDataResponse(
    val id: Long,
    val sector: String,
    val lat: Double,
    val lng: Double,
)
