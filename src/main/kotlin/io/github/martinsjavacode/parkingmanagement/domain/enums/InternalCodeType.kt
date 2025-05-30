package io.github.martinsjavacode.parkingmanagement.domain.enums

enum class InternalCodeType(val code: String, val messageKey: String) {
    // Parking Errors
    PARKING_NOT_SAVED("PRK-001", "parking.not.saved"),
    PARKING_NOT_FOUND("PRK-002", "parking.not.found"),

    // Parking Event Erros
    PARKING_EVENT_LICENSE_PLATE_NOT_FOUND("PVN-002", "parking.event.license.plate.not.found"),
    PARKING_EVENT_LICENSE_PLATE_ALRIGHT_EXISTS("PVN-003", "parking.event.license.plate.alright.exists"),

    // Webhook Event
    WEBHOOK_PARKED_EVENT_ALREADY_EXISTS("WBH-001", "webhook.parked.event.already.exists"),
    WEBHOOK_ENTRY_EVENT_NOT_FOUND("WBH-002", "webhook.entry.event.not.found"),
    WEBHOOK_ENTRY_LICENSE_PLATE_CONFLICT("WBH-003", "webhook.entry.license.plate.conflict"),
    WEBHOOK_ENTRY_NO_PARKING_OPEN("WBH-004", "webhook.entry.no.parking.open"),

    // General Errors
    UNEXPECTED_DATABASE_ERROR("DFL-001", "unexpected.database.error");

    fun code(): String = code

    fun messageKey(): String = messageKey
}
