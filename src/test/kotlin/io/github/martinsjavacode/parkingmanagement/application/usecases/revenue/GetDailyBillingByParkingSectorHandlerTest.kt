package io.github.martinsjavacode.parkingmanagement.application.usecases.revenue

import io.github.martinsjavacode.parkingmanagement.application.usecases.revenue.impl.GetDailyBillingByParkingSectorHandlerImpl
import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.revenue.RevenueRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.RevenueNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

class GetDailyBillingByParkingSectorHandlerTest : DescribeSpec({

    val messageSource =
        mockk<MessageSource> {
            coEvery { getMessage(any(), any(), any()) } returns "Error message"
        }
    val traceContext =
        mockk<TraceContext> {
            coEvery { traceId() } returns "trace-123"
        }
    val parkingRepository = mockk<ParkingRepositoryPort>()
    val revenueRepository = mockk<RevenueRepositoryPort>()

    val handler =
        GetDailyBillingByParkingSectorHandlerImpl(
            messageSource = messageSource,
            traceContext = traceContext,
            parkingRepository = parkingRepository,
            revenueRepository = revenueRepository,
        )

    describe("GetDailyBillingByParkingSectorHandler") {
        val date = LocalDate.now()
        val sectorName = "A"
        val parkingId = 1L

        val parking =
            Parking(
                id = parkingId,
                sector = sectorName,
                basePrice = BigDecimal("10.00"),
                maxCapacity = 100,
                openHour = LocalTime.of(8, 0),
                closeHour = LocalTime.of(20, 0),
                durationLimitMinutes = 120,
                spots = flowOf(),
            )

        val revenue =
            Revenue(
                id = 1L,
                parkingId = parkingId,
                date = date,
                amount = BigDecimal("150.00"),
                currency = CurrencyType.BRL,
            )

        it("should return revenue when it exists") {
            // Given
            coEvery { parkingRepository.findBySectorName(sectorName) } returns parking
            coEvery { revenueRepository.getRevenueForParkingOnDate(parkingId, date) } returns revenue

            // When
            val result = handler.handle(date, sectorName)

            // Then
            result shouldBe revenue
        }

        it("should throw RevenueNotFoundException when revenue does not exist") {
            // Given
            coEvery { parkingRepository.findBySectorName(sectorName) } returns parking
            coEvery { revenueRepository.getRevenueForParkingOnDate(parkingId, date) } returns null

            // When/Then
            shouldThrow<RevenueNotFoundException> {
                handler.handle(date, sectorName)
            }
        }
    }
})
