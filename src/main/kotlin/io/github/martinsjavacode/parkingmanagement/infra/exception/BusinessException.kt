package io.github.martinsjavacode.parkingmanagement.infra.exception

import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import java.lang.RuntimeException

/**
 * Base exception for business errors in the system.
 *
 * This class serves as the foundation for all specific business exceptions,
 * providing a standardized format for error codes, messages, and tracing.
 *
 * @property code Internal error code
 * @property message Technical message detailing the error
 * @property friendlyMessage User-friendly message for display
 * @property traceId Trace identifier for log correlation
 * @property type Exception type
 */
open class BusinessException(
    open val httpStatus: HttpStatus = BAD_REQUEST,
    open val internalCode: String,
    override val message: String,
    open val friendlyMessage: String?,
    open val internalTraceId: String?,
    open val type: ExceptionType,
) : RuntimeException(message)
