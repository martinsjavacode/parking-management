package io.github.martinsjavacode.parkingmanagement.infra.persistence.handler

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.UNEXPECTED_DATABASE_ERROR
import io.github.martinsjavacode.parkingmanagement.domain.exception.UnexpectedDatabaseException
import io.github.martinsjavacode.parkingmanagement.loggerFor
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

@Component
class PersistenceHandler(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
) {
    private val logger = loggerFor<PersistenceHandler>()

    suspend fun <T> handleOperation(operation: suspend () -> T): T =
        runCatching {
            operation()
        }.onFailure { exception ->
            logger.error("Error during database operation: ${exception.message}", exception)
        }.getOrElse { exception ->
            throw UnexpectedDatabaseException(
                internalCode = UNEXPECTED_DATABASE_ERROR.code(),
                message =
                    messageSource.getMessage(
                        UNEXPECTED_DATABASE_ERROR.messageKey(),
                        null,
                        LocaleContextHolder.getLocale(),
                    ),
                friendlyMessage =
                    messageSource.getMessage(
                        "${UNEXPECTED_DATABASE_ERROR.messageKey()}.friendly",
                        null,
                        LocaleContextHolder.getLocale(),
                    ),
                internalTraceId = traceContext.traceId(),
                type = ExceptionType.UNEXPECTED,
            )
        }
}
