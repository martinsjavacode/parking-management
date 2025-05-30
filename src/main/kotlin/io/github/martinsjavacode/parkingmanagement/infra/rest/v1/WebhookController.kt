package io.github.martinsjavacode.parkingmanagement.infra.rest.v1

import io.github.martinsjavacode.parkingmanagement.domain.model.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.service.webhook.ParkingWebhookHandler
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/webhook")
class WebhookController(
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
