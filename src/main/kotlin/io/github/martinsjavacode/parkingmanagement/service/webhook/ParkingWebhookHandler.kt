package io.github.martinsjavacode.parkingmanagement.service.webhook

import io.github.martinsjavacode.parkingmanagement.domain.model.WebhookEvent

interface ParkingWebhookHandler {
    suspend fun execute(event: WebhookEvent)
}
