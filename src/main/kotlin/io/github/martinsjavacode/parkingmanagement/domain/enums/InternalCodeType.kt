package io.github.martinsjavacode.parkingmanagement.domain.enums

enum class InternalCodeType(val code: String, val messageKey: String) {
    // Parking Errors
    PARKING_NOT_SAVED("PRK-001", "parking.not.saved"),
    PARKING_NOT_FOUND("PRK-002", "parking.not.found"),

    // Parking Event Erros
    PARKING_EVENT_LICENSE_PLATE_NOT_FOUND("PVN-001", "parking.event.license.plate.not.found"),
    PARKING_EVENT_NOT_SAVED("PVN-002", "parking.event.not.saved"),

    // PARKING_SPOT
    PARKING_SPOT_NOT_FOUND("PST-001", "parking.spot.not.found"),

    // Webhook Event
    WEBHOOK_PARKED_EVENT_ALREADY_EXISTS("WBH-001", "webhook.parked.event.already.exists"),
    WEBHOOK_CODE_EVENT_NOT_FOUND("WBH-002", "webhook.code.event.not.found"),
    WEBHOOK_ENTRY_LICENSE_PLATE_CONFLICT("WBH-003", "webhook.entry.license.plate.conflict"),
    WEBHOOK_ENTRY_NO_PARKING_OPEN("WBH-004", "webhook.entry.no.parking.open"),

    // REVENUE
    DAILY_REVENUE_NOT_FOUND("RVN-001", "daily.revenue.not.found"),
    REVENUE_NOT_SAVED("RVN-002", "revenue.not.saved"),


    // General Errors
    UNEXPECTED_DATABASE_ERROR("DFL-001", "unexpected.database.error"),
    ;

    fun code(): String = code

    fun messageKey(): String = messageKey
}
