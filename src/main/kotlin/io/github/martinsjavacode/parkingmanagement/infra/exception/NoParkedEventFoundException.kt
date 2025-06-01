package io.github.martinsjavacode.parkingmanagement.infra.exception

import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import org.springframework.http.HttpStatus

/**
 * Exception thrown when a parking event is not found.
 *
 * This exception is raised when an EXIT event is received,
 * but there is no corresponding PARKED event for the license plate.
 *
 * @param code Internal error code
 * @param message Technical message detailing the error
 * @param friendlyMessage User-friendly message for display
 * @param traceId Trace identifier for log correlation
 * @param type Exception type
 */
class NoParkedEventFoundException(
    override val internalCode: String,
    override val message: String,
    override val friendlyMessage: String? = null,
    override val internalTraceId: String?,
    override val type: ExceptionType,
) : BusinessException(
        HttpStatus.UNPROCESSABLE_ENTITY,
        internalCode,
        message,
        friendlyMessage,
        internalTraceId,
        type,
    )
