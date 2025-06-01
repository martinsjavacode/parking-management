package io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.ParkingWebhookHandler
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent
import org.springframework.stereotype.Service

/**
 * Implementation of the webhook handler for parking events.
 *
 * This class routes received events to specific handlers
 * based on the event type (ENTRY, PARKED, EXIT).
 *
 * @property entryWebhookHandler Handler for entry events
 * @property parkedWebhookHandler Handler for parking events
 * @property exitWebhookHandler Handler for exit events
 */
@Service
class ParkingWebhookHandlerImpl(
    private val entryWebhookHandler: EntryWebhookHandler,
    private val parkedWebhookHandler: ParkedWebhookHandler,
    private val exitWebhookHandler: ExitWebhookHandler,
) : ParkingWebhookHandler {
    /**
     * Executes the processing of the webhook event, routing it to the appropriate handler.
     *
     * @param event The webhook event to be processed
     */
    override suspend fun execute(event: WebhookEvent) {
        when (event.eventType) {
            EventType.ENTRY -> entryWebhookHandler.handle(event)
            EventType.PARKED -> parkedWebhookHandler.handle(event)
            EventType.EXIT -> exitWebhookHandler.handle(event)
        }
    }
}
