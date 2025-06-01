package io.github.martinsjavacode.parkingmanagement.infra.exception

import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import org.springframework.http.HttpStatus

/**
 * Exception thrown when there is a vehicle license plate conflict.
 *
 * This exception is raised when an ENTRY event is received,
 * but an active event already exists for the same license plate.
 *
 * @param code Internal error code
 * @param message Technical message detailing the error
 * @param friendlyMessage User-friendly message for display
 * @param traceId Trace identifier for log correlation
 * @param type Exception type
 */
class LicensePlateConflictException(
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
