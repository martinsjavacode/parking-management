package io.github.martinsjavacode.parkingmanagement.domain.exception

import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import org.springframework.http.HttpStatus

class LicensePlateAlreadyExistsException(
    override val internalCode: String,
    override val message: String,
    override val friendlyMessage: String? = null,
    override val internalTraceId: String?,
    override val type: ExceptionType,
) : BusinessException(
        HttpStatus.BAD_REQUEST,
        internalCode,
        message,
        friendlyMessage,
        internalTraceId,
        type,
    )
