package io.github.martinsjavacode.parkingmanagement.infra.exception

import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import org.springframework.http.HttpStatus

/**
 * Exception thrown when a duplicate event is detected in an idempotent operation.
 *
 * This exception is raised when an attempt is made to process a new event
 * (e.g., PARKED), but the operation violates the idempotency rules.
 * Typically, this occurs when a request is repeated, and the system identifies
 * that a conflicting event already exists for the given parameters (e.g., license plate,
 * location, or event ID).
 *
 * Idempotency ensures that repeated operations with the same inputs do not
 * cause unintended side effects or duplicate processing.
 *
 * The HTTP status code associated with this exception is `409 Conflict`.
 *
 * @param internalCode Internal error code that identifies this specific error type.
 * @param message A detailed technical message describing the idempotency violation.
 * @param friendlyMessage A user-friendly message intended for display purposes.
 * @param internalTraceId A unique trace identifier to correlate logs and debug issues.
 * @param type The category or type of the exception (e.g., VALIDATION, BUSINESS_LOGIC).
 */
class DuplicateEventException(
    override val internalCode: String,
    override val message: String,
    override val friendlyMessage: String? = null,
    override val internalTraceId: String?,
    override val type: ExceptionType,
) : BusinessException(
        HttpStatus.CONFLICT,
        internalCode,
        message,
        friendlyMessage,
        internalTraceId,
        type,
    )
