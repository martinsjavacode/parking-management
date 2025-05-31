package io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.ParkingWebhookHandler
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent
import org.springframework.stereotype.Service

@Service
class ParkingWebhookHandlerImpl(
    private val entryWebhookHandler: EntryWebhookHandler,
    private val parkedWebhookHandler: ParkedWebhookHandler,
    private val exitWebhookHandler: ExitWebhookHandler,
) : ParkingWebhookHandler {
    override suspend fun execute(event: WebhookEvent) {
        when (event.eventType) {
            EventType.ENTRY -> entryWebhookHandler.handle(event)
            EventType.PARKED -> parkedWebhookHandler.handle(event)
            EventType.EXIT -> exitWebhookHandler.handle(event)
        }
    }
}
