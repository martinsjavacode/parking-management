package io.github.martinsjavacode.parkingmanagement.service.webhook

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.WebhookEvent
import org.springframework.stereotype.Service

@Service
class ParkingWebhookHandler(
    private val entryWebhookHandler: EntryWebhookHandler,
    private val parkedWebhookHandler: ParkedWebhookHandler,
    private val exitWebhookHandler: ExitWebhookHandler,
) {
    suspend fun execute(event: WebhookEvent) {
        when (event.eventType) {
            EventType.ENTRY -> entryWebhookHandler.handle(event)
            EventType.PARKED -> parkedWebhookHandler.handle(event)
            EventType.EXIT -> exitWebhookHandler.handle(event)
        }
    }
}
