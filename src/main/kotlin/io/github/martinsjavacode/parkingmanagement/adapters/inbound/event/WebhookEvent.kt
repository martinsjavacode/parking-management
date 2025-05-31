package io.github.martinsjavacode.parkingmanagement.adapters.inbound.event

import io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.ParkingWebhookHandler
import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/webhook")
class WebhookEvent(
    private val webHookHandlerParking: ParkingWebhookHandler,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    suspend fun handleWebhook(
        @RequestBody event: WebhookEvent,
    ) {
        webHookHandlerParking.execute(event)
    }
}
