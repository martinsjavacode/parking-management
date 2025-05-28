package io.github.martinsjavacode.parkingmanagement.domain.enums

enum class InternalCodeType(val code: String, val messageKey: String) {
    // Parking Errors
    PARKING_NOT_FOUND("P-001", "parking.not-found"),
    PARKING_ALREADY_EXISTS("P-002", "parking.already-exists"),
    PARKING_NOT_SAVED("P-003", "parking.not-saved"),

    // Parking Spot Errors
    PARKING_SPOT_NOT_FOUND("PSP-001", "parking-spot.not-found"),
    PARKING_SPOT_ALREADY_TAKEN("PSP-002", "parking-spot.already-taken"),
    PARKING_SPOT_NOT_AVAILABLE("PSP-003", "parking-spot.not-available"),


    // General Errors
    BAD_REQUEST("DFL-001", "default.bad-request"),
    UNAUTHORIZED("DFL-002", "default.unauthorized"),
    FORBIDDEN("DFL-003", "default.forbidden"),
    NOT_FOUND("DFL-004", "default.not-found"),
    METHOD_NOT_ALLOWED("DFL-005", "default.method-not-allowed"),
    CONFLICT("DFL-006", "default.conflict"),
    UNPROCESSABLE_ENTITY("DFL-007", "default.unprocessable-entity");

    fun code(): String = code
    fun messageKey(): String = messageKey
}

