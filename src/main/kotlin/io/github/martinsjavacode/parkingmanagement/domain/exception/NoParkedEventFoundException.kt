package io.github.martinsjavacode.parkingmanagement.domain.exception

import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import org.springframework.http.HttpStatus

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
