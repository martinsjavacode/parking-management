package io.github.martinsjavacode.parkingmanagement.application.usecases.webhook

import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent

interface ParkingWebhookHandler {
    suspend fun execute(event: WebhookEvent)
}
