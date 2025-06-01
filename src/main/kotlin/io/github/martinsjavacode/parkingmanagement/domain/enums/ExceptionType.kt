package io.github.martinsjavacode.parkingmanagement.domain.enums

/**
 * Enum representing the types of exceptions in the system.
 *
 * Defines the categories of exceptions that can occur during execution.
 */
enum class ExceptionType {
    /**
     * Validation exception.
     * Occurs when the provided data does not meet the requirements.
     */
    VALIDATION,

    /**
     * Business exception.
     * Occurs when a business rule is violated.
     */
    BUSINESS,

    /**
     * Persistence request exception.
     * Occurs during database operations.
     */
    PERSISTENCE_REQUEST,

    /**
     * System exception.
     * Occurs due to internal system issues.
     */
    SYSTEM,
}
