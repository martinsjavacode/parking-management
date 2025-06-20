package io.github.martinsjavacode.parkingmanagement.infra.exception

import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import org.springframework.http.HttpStatus.BAD_REQUEST

class ParkingSpotSaveFailedException(
    override val internalCode: String,
    override val message: String,
    override val friendlyMessage: String? = null,
    override val internalTraceId: String?,
    override val type: ExceptionType,
) : BusinessException(
        BAD_REQUEST,
        internalCode,
        message,
        friendlyMessage,
        internalTraceId,
        type,
    )
