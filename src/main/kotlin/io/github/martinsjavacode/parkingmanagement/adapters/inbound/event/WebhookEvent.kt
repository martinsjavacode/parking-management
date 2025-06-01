package io.github.martinsjavacode.parkingmanagement.adapters.inbound.event

import io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.ParkingWebhookHandler
import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * REST Controller to receive webhook events.
 *
 * Provides an endpoint to receive events from external systems,
 * such as parking simulators.
 *
 * @property webHookHandlerParking Handler to process webhook events
 */
@RestController
@RequestMapping("/webhook")
class WebhookEvent(
    private val webHookHandlerParking: ParkingWebhookHandler,
) {
    /**
     * Endpoint to receive and process webhook events.
     *
     * @param event The webhook event to process
     */
    @Operation(
        summary = "Handle parking events",
        tags = ["Webhooks"],
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Details of the parking event",
                required = true,
                content = [
                    io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        schema = io.swagger.v3.oas.annotations.media.Schema(implementation = WebhookEvent::class),
                    ),
                ],
            ),
        responses = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "Event processed successfully",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid event data",
                content = [
                    io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        schema = io.swagger.v3.oas.annotations.media.Schema(),
                    ),
                ],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [
                    io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        schema = io.swagger.v3.oas.annotations.media.Schema(),
                    ),
                ],
            ),
        ],
    )
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    suspend fun handleWebhook(
        @RequestBody event: WebhookEvent,
    ) {
        webHookHandlerParking.execute(event)
    }
}
