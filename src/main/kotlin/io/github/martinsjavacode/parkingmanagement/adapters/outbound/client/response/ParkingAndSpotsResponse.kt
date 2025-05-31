package io.github.martinsjavacode.parkingmanagement.adapters.outbound.client.response

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect
data class ParkingAndSpotsResponse(
    @JsonProperty("garage")
    val parking: List<ParkingDataResponse>,
    @JsonProperty("spots")
    val parkingSpots: List<ParkingSpotDataResponse>,
)
