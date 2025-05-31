package io.github.martinsjavacode.parkingmanagement.infra.exception

import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import java.lang.RuntimeException

open class BusinessException(
    open val httpStatus: HttpStatus = BAD_REQUEST,
    open val internalCode: String,
    override val message: String,
    open val friendlyMessage: String?,
    open val internalTraceId: String?,
    open val type: ExceptionType,
) : RuntimeException(message)
