package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.revenue

import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.revenue.request.DailyBillingRequest
import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.revenue.response.DailyBillingResponse
import io.github.martinsjavacode.parkingmanagement.application.usecases.revenue.GetDailyBillingByParkingSectorHandler
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
