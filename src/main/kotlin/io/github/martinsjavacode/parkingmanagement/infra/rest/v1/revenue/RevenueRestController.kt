package io.github.martinsjavacode.parkingmanagement.infra.rest.v1.revenue

import io.github.martinsjavacode.parkingmanagement.infra.rest.v1.revenue.request.DailyBillingRequest
import io.github.martinsjavacode.parkingmanagement.infra.rest.v1.revenue.response.DailyBillingResponse
import io.github.martinsjavacode.parkingmanagement.service.revenue.GetDailyBillingByParkingSectorHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/revenue")
class RevenueRestController(
    private val getDailyBillingByParkingSectorHandler: GetDailyBillingByParkingSectorHandler
) {

    @GetMapping
    suspend fun billingConsultation(
        @RequestBody body: DailyBillingRequest
    ): ResponseEntity<DailyBillingResponse> {
        val revenue = getDailyBillingByParkingSectorHandler.handle(body.date, body.sector)
        val dailyBillingResponse = DailyBillingResponse(
            amount = revenue.amount,
            currency = revenue.currency,
            timestamp = revenue.date.atStartOfDay()
        )

        return ResponseEntity.ok(dailyBillingResponse)
    }
}
