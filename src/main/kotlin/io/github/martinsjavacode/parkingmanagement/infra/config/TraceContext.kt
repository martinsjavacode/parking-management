package io.github.martinsjavacode.parkingmanagement.infra.config

import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Component for generating and managing trace IDs for request tracking.
 *
 * This class provides functionality to generate unique trace IDs
 * that can be used to track requests across the system.
 */
@Component
class TraceContext() {
    /**
     * Generates a unique trace ID.
     *
     * @return A string representation of a UUID to be used as a trace ID
     */
    fun traceId(): String? = UUID.randomUUID().toString()
}
