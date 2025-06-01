package io.github.martinsjavacode.parkingmanagement.infra.exception

import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import org.springframework.http.HttpStatus

/**
 * Exception thrown when there are no open parking lots.
 *
 * This exception is raised when an ENTRY event is received,
 * but there are no open parking lots at the moment.
 *
 * @param code Internal error code
 * @param message Technical message detailing the error
 * @param friendlyMessage User-friendly message for display
 * @param traceId Trace identifier for log correlation
 * @param type Exception type
 */
class NoParkingOpenException(
    override val internalCode: String,
    override val message: String,
    override val friendlyMessage: String? = null,
    override val internalTraceId: String?,
    override val type: ExceptionType,
) : BusinessException(
        HttpStatus.NOT_FOUND,
        internalCode,
        message,
        friendlyMessage,
        internalTraceId,
        type,
    )
