package io.github.martinsjavacode.parkingmanagement.domain.enums

/**
 * Enum representing the types of parking events.
 *
 * Defines the possible states of a vehicle in the parking system.
 */
enum class EventType {
    /**
     * Vehicle entry event into the parking lot.
     * Recorded when the vehicle passes through the entrance gate.
     */
    ENTRY,
    /**
     * Vehicle parking event in a parking spot.
     * Recorded when the vehicle is detected in a specific spot.
     */
    PARKED,
    /**
     * Vehicle exit event from the parking lot.
     * Recorded when the vehicle passes through the exit gate.
     */
    EXIT,
}
