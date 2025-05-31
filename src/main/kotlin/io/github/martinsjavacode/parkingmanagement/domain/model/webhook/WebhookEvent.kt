package io.github.martinsjavacode.parkingmanagement.domain.model.webhook

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import java.time.LocalDateTime

@JsonAutoDetect
data class WebhookEvent(
    @JsonProperty("license_plate")
    val licensePlate: String,
    val lat: Double?,
    val lng: Double?,
    @JsonProperty("entry_time")
    val entryTime: LocalDateTime?,
    @JsonProperty("exit_time")
    val exitTime: LocalDateTime?,
    @JsonProperty("event_type")
    val eventType: EventType,
)
