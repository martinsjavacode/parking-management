package io.github.martinsjavacode.parkingmanagement.domain.enums

enum class InternalCodeType(val code: String, val messageKey: String) {
    // Parking Errors
    PARKING_NOT_SAVED("PRK-001", "parking.not.saved"),

    // Parking Spot Errors
    PARKING_SPOT_NOT_FOUND("PSP-001", "parking-spot.not.found"),

    // Parking Event Erros
    PARKING_EVENT_TYPE_INVALID("PVN-001", "parking.event.type.invalid"),
    PARKING_EVENT_LICENSE_PLATE_NOT_FOUND("PVN-002", "parking.event.license.plate.not.found"),

    // General Errors
    UNEXPECTED_DATABASE_ERROR("DFL-001", "error.unexpected.database"),
    ;

    fun code(): String = code

    fun messageKey(): String = messageKey
}
