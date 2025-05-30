package io.github.martinsjavacode.parkingmanagement.config

import io.github.martinsjavacode.parkingmanagement.domain.exception.BusinessException
import io.github.martinsjavacode.parkingmanagement.loggerFor
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.Logger
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import reactor.core.publisher.Mono

@RestControllerAdvice
class GlobalExceptionHandler(
    private val meterRegistry: MeterRegistry,
    private val traceContext: TraceContext,
) {
    val logger: Logger = loggerFor<GlobalExceptionHandler>()

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): Mono<ResponseEntity<Map<String, Any?>>> {
        // Metrics for exceptions
        Mono.fromCallable {
            meterRegistry
                .counter(
                    "exceptions.count",
                    "type",
                    ex::class.simpleName ?: "Unknown",
                ).increment()
        }.subscribe()


        val logDetails =
            mapOf(
                "error" to ex.message,
                "httpStatus" to ex.httpStatus,
                "exceptionType" to ex::class.simpleName,
                "internalCode" to ex.internalCode,
                "internalTraceId" to ex.internalTraceId,
                "type" to ex.type,
            )
        logger.error("Business exception occurred. {}", logDetails)

        val body =
            mapOf(
                "internalCode" to ex.internalCode,
                "message" to ex.message,
                "friendlyMessage" to ex.friendlyMessage,
                "internalTraceId" to ex.internalTraceId,
                "type" to ex.type,
            )
        return Mono.just(ResponseEntity.status(ex.httpStatus).body(body))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): Mono<ResponseEntity<Map<String, Any>>> {
        val details = ex.message ?: "No details"
        val errorMessage = ex.localizedMessage ?: "Unexpected error occurred"

        val logDetails =
            mapOf(
                "details" to details,
                "traceId" to traceContext.traceId(),
                "exceptionType" to ex::class.simpleName,
            )
        logger.error(errorMessage, logDetails)

        val body =
            mapOf(
                "error" to errorMessage,
                "details" to details,
            )
        return Mono.just(ResponseEntity.status(INTERNAL_SERVER_ERROR).body(body))
    }
}
