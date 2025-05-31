package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.revenue

import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.revenue.request.DailyBillingRequest
import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.revenue.response.DailyBillingResponse
import io.github.martinsjavacode.parkingmanagement.application.usecases.revenue.GetDailyBillingByParkingSectorHandler
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalTime

@RestController
@RequestMapping("/revenue")
class RevenueRestController(
    private val getDailyBillingByParkingSectorHandler: GetDailyBillingByParkingSectorHandler,
) {
    @Operation(
        summary = "Get accumulated revenue by sector and date",
        tags = ["Revenue"],
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Query revenue by sector and date",
                required = true,
                content = [
                    io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        schema =
                            io.swagger.v3.oas.annotations.media.Schema(
                                implementation = DailyBillingRequest::class,
                            ),
                    ),
                ],
            ),
        responses = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Revenue retrieved successfully",
                content = [
                    io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        schema =
                            io.swagger.v3.oas.annotations.media.Schema(
                                implementation = DailyBillingResponse::class,
                            ),
                    ),
                ],
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
    @GetMapping
    suspend fun billingConsultation(
        @RequestBody request: DailyBillingRequest,
    ): ResponseEntity<DailyBillingResponse> {
        val revenue = getDailyBillingByParkingSectorHandler.handle(request.date, request.sector)
        val dailyBillingResponse =
            DailyBillingResponse(
                amount = revenue.amount,
                currency = revenue.currency,
                timestamp = revenue.date.atTime(LocalTime.now()),
            )

        return ResponseEntity.ok(dailyBillingResponse)
    }
}
