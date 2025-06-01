package io.github.martinsjavacode.parkingmanagement.application.usecases.webhook

import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent

/**
 * Interface for processing parking-related webhook events.
 *
 * Defines the contract for handling events received via webhook
 * from external systems, such as parking simulators.
 */
interface ParkingWebhookHandler {
    /**
     * Processes a webhook event.
     *
     * @param event The webhook event to be processed
     */
    suspend fun execute(event: WebhookEvent)
}
