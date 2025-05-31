package io.github.martinsjavacode.parkingmanagement.domain.model.webhook

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonAutoDetect
data class WebhookEvent(
    @JsonProperty("license_plate")
    @field:Schema(description = "License plate of the vehicle", example = "ZUL0001", required = true)
    val licensePlate: String,
    @field:Schema(description = "Latitude of the vehicle", example = "-23.5505199", required = false)
    val lat: Double?,
    @field:Schema(description = "Longitude of the vehicle", example = "-46.6333094", required = false)
    val lng: Double?,
    @field:Schema(description = "Time of vehicle entry", example = "2025-01-01T12:00:00.000Z", required = false)
    @JsonProperty("entry_time")
    val entryTime: LocalDateTime?,
    @field:Schema(description = "Time of vehicle departure", example = "2025-01-01T12:00:00.000Z", required = false)
    @JsonProperty("exit_time")
    val exitTime: LocalDateTime?,
    @field:Schema(
        description = "Type of the event",
        example = "ENTRY",
        required = true,
        allowableValues = ["ENTRY", "PARKED", "EXIT"],
    )
    @JsonProperty("event_type")
    val eventType: EventType,
)
