package io.github.martinsjavacode.parkingmanagement.infra.config

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TraceContext() {
    fun traceId(): String? = UUID.randomUUID().toString()
}
