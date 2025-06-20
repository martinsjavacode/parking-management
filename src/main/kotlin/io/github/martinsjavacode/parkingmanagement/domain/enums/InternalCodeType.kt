package io.github.martinsjavacode.parkingmanagement.domain.enums

/**
 * Enum representing internal error codes in the system.
 *
 * Defines specific codes for different types of errors,
 * aiding in identification and handling.
 */
enum class InternalCodeType(val code: String, val messageKey: String) {
    // Parking
    PARKING_NOT_SAVED("PRK-001", "parking.not.saved"),
    PARKING_NOT_FOUND("PRK-002", "parking.not.found"),

    // Parking Event
    PARKING_EVENT_LICENSE_PLATE_NOT_FOUND("PVN-001", "parking.event.license.plate.not.found"),
    PARKING_EVENT_NOT_SAVED("PVN-002", "parking.event.not.saved"),
    PARKING_EVENT_NOT_FOUND("PVN-003", "parking.event.not.found"),
    PARKING_EVENT_DUPLICATE_EVENT("PVN-004", "parking.event.duplicate.event"),

    // PARKING_SPOT
    PARKING_SPOT_NOT_FOUND("PST-001", "parking.spot.not.found"),
    PARKING_SPOT_NOT_SAVED("PST-002", "parking.spot.not.saved"),
    PARKING_SPOT_OCCUPIED("PST-002", "parking.spot.occupied"),

    // Webhook Event
    WEBHOOK_EVENT_ACTIVE_NOT_FOUND("WBH-002", "webhook.event.active.not.found"),
    WEBHOOK_ENTRY_LICENSE_PLATE_CONFLICT("WBH-003", "webhook.entry.license.plate.conflict"),
    WEBHOOK_ENTRY_NO_PARKING_OPEN("WBH-004", "webhook.entry.no.parking.open"),

    // REVENUE
    REVENUE_NOT_FOUND("RVN-001", "revenue.not.found"),
    REVENUE_NOT_SAVED("RVN-002", "revenue.not.saved"),

    ;

    fun code(): String = code

    fun messageKey(): String = messageKey
}
