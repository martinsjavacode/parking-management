package io.github.martinsjavacode.parkingmanagement.domain.gateway.client.response

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonAutoDetect
data class ParkingDataResponse(
    val sector: String,
    @JsonProperty("base_price")
    val basePrice: BigDecimal,
    @JsonProperty("max_capacity")
    val maxCapacity: Int,
    @JsonProperty("open_hour")
    val openHour: String,
    @JsonProperty("close_hour")
    val closeHour: String,
    @JsonProperty("duration_limit_minutes")
    val durationLimitMinutes: Int,
)
