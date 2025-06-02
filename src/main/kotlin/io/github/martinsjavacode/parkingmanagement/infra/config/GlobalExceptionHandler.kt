package io.github.martinsjavacode.parkingmanagement.infra.config

import io.github.martinsjavacode.parkingmanagement.infra.exception.BusinessException
import io.github.martinsjavacode.parkingmanagement.loggerFor
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import reactor.core.publisher.Mono

/**
 * Global exception handler for the application.
 *
 * This class provides centralized exception handling across all controllers
 * in the application, ensuring consistent error responses.
 *
 * @property meterRegistry Registry for metrics collection
 * @property traceContext Context for tracing requests
 */
@RestControllerAdvice
class GlobalExceptionHandler(
    private val meterRegistry: MeterRegistry,
    private val traceContext: TraceContext,
) {
    val logger = loggerFor<GlobalExceptionHandler>()

    /**
     * Handles business exceptions thrown by the application.
     *
     * @param ex The business exception to handle
     * @return A response entity with appropriate status and error details
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): Mono<ResponseEntity<Map<String, Any?>>> {
        // Metrics for exceptions
        meterRegistry
            .counter(
                "exceptions.count",
                "type",
                ex::class.simpleName ?: "Unknown",
            ).increment()

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

    /**
     * Handles generic exceptions not specifically handled elsewhere.
     *
     * @param ex The exception to handle
     * @return A response entity with 500 status and error details
     */
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
