package io.github.martinsjavacode.parkingmanagement.domain.model.webhook

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents an event received via a webhook from external systems.
 *
 * This class contains raw data received from external systems,
 * such as parking simulators, for processing by the system.
 *
 * @property id Unique identifier for the event, used for idempotency
 * @property licensePlate License plate of the vehicle associated with the event
 * @property eventType Type of the event (ENTRY, PARKED, EXIT)
 * @property timestamp Timestamp of the event, mainly used for entry events
 * @property entryTime Entry time, used for entry events
 * @property exitTime Exit time, used for exit events
 * @property lat Latitude coordinate where the event occurred
 * @property lng Longitude coordinate where the event occurred
 */
@JsonAutoDetect
data class WebhookEvent(
    @field:Schema(
        description = "Unique identifier for the event",
        example = "123e4567-e89b-12d3-a456-426614174000",
        required = false,
    )
    val id: String? = UUID.randomUUID().toString(),
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
