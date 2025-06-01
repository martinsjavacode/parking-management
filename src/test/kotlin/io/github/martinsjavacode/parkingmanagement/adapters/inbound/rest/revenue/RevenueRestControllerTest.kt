package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.revenue

import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.revenue.request.DailyBillingRequest
import io.github.martinsjavacode.parkingmanagement.application.usecases.revenue.GetDailyBillingByParkingSectorHandler
import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDate

class RevenueRestControllerTest : BehaviorSpec({
    val getDailyBillingByParkingSectorHandler = mockk<GetDailyBillingByParkingSectorHandler>()

    val revenueRestController =
        RevenueRestController(
            getDailyBillingByParkingSectorHandler = getDailyBillingByParkingSectorHandler,
        )

    beforeTest {
        clearAllMocks()
    }

    given("A daily billing request") {
        val date = LocalDate.of(2025, 1, 1)
        val sector = "A"
        val request =
            DailyBillingRequest(
                date = date,
                sector = sector,
            )

        val revenue =
            Revenue(
                id = 1L,
                parkingId = 1L,
                date = date,
                amount = BigDecimal("150.00"),
                currency = CurrencyType.BRL,
            )

        `when`("The handler returns revenue data") {
            beforeTest {
                coEvery {
                    getDailyBillingByParkingSectorHandler.handle(date, sector)
                } returns revenue
            }

            then("Should return a successful response with the revenue data") {
                val response = revenueRestController.billingConsultation(request)

                // Verify the response status
                response.statusCode shouldBe HttpStatus.OK

                // Verify the response body
                val responseBody = response.body!!
                responseBody.amount shouldBe BigDecimal("150.00")
                responseBody.currency shouldBe CurrencyType.BRL

                // We can't directly compare the timestamp because it uses LocalTime.now()
                // So we'll just check that it has the correct date
                responseBody.timestamp.toLocalDate() shouldBe date
            }
        }

        `when`("The handler throws an exception") {
            val exception = RuntimeException("Revenue not found")

            beforeTest {
                coEvery {
                    getDailyBillingByParkingSectorHandler.handle(date, sector)
                } throws exception
            }

            then("Should propagate the exception") {
                val thrownException =
                    runCatching {
                        revenueRestController.billingConsultation(request)
                    }.exceptionOrNull()

                thrownException shouldBe exception
            }
        }
    }
})
