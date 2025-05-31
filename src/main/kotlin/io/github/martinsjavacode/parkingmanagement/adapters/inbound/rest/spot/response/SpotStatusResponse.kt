package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.spot.response

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

@JsonAutoDetect
data class SpotStatusResponse(
    val ocupied: Boolean,
    @JsonProperty("license_plate")
    val licensePlate: String,
    @JsonProperty("price_until_now")
    val priceUntilNow: BigDecimal,
    @JsonProperty("entry_time")
    val entryTime: LocalDateTime,
    @JsonProperty("time_parked")
    val timeParked: LocalDateTime
)

