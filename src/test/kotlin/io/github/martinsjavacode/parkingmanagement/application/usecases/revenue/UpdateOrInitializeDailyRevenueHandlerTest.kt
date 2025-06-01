package io.github.martinsjavacode.parkingmanagement.application.usecases.revenue

import io.github.martinsjavacode.parkingmanagement.application.usecases.revenue.impl.UpdateOrInitializeDailyRevenueHandlerImpl
import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
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

class UpdateOrInitializeDailyRevenueHandlerTest : DescribeSpec({

    val messageSource =
        mockk<MessageSource> {
            coEvery { getMessage(any(), any(), any()) } returns "Error message"
        }
    val traceContext =
        mockk<TraceContext> {
            coEvery { traceId() } returns "trace-123"
        }
    val parkingCustomQueryRepository = mockk<ParkingCustomQueryRepositoryPort>()
    val revenueRepository = mockk<RevenueRepositoryPort>()

    val handler =
        UpdateOrInitializeDailyRevenueHandlerImpl(
            messageSource = messageSource,
            traceContext = traceContext,
            parkingCustomQueryRepository = parkingCustomQueryRepository,
            revenueRepository = revenueRepository,
        )

    describe("UpdateOrInitializeDailyRevenueHandler") {
        val latitude = 10.123456
        val longitude = -20.654321
        val parkingId = 1L
        val today = LocalDate.now()

        val parking =
            Parking(
                id = parkingId,
                sector = "A",
                basePrice = BigDecimal("10.00"),
                maxCapacity = 100,
                openHour = LocalTime.of(8, 0),
                closeHour = LocalTime.of(20, 0),
                durationLimitMinutes = 120,
                spots = flowOf(),
            )

        val existingRevenue =
            Revenue(
                id = 1L,
                parkingId = parkingId,
                date = today,
                amount = BigDecimal("150.00"),
                currency = CurrencyType.BRL,
            )

        val newRevenue =
            Revenue(
                parkingId = parkingId,
                date = today,
                amount = BigDecimal.ZERO,
                currency = CurrencyType.BRL,
            )

        val savedNewRevenue = newRevenue.copy(id = 2L)

        context("when handling PARKED event") {
            it("should initialize daily revenue when it doesn't exist") {
                // Given
                coEvery { parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude) } returns parking
                coEvery { revenueRepository.getRevenueForParkingOnDate(parkingId, today) } returns null
                coEvery { revenueRepository.upsert(any()) } returns savedNewRevenue

                // When
                val result =
                    handler.handle(
                        eventType = EventType.PARKED,
                        latitude = latitude,
                        longitude = longitude,
                    )

                // Then
                result shouldBe savedNewRevenue
            }

            it("should return existing revenue when it already exists") {
                // Given
                coEvery { parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude) } returns parking
                coEvery { revenueRepository.getRevenueForParkingOnDate(parkingId, today) } returns existingRevenue

                // When
                val result =
                    handler.handle(
                        eventType = EventType.PARKED,
                        latitude = latitude,
                        longitude = longitude,
                    )

                // Then
                result shouldBe existingRevenue
            }
        }

        context("when handling EXIT event") {
            val amountPaid = BigDecimal("25.00")
            val updatedRevenue = existingRevenue.copy(amount = existingRevenue.amount.add(amountPaid))

            it("should update daily revenue when it exists") {
                // Given
                coEvery { parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude) } returns parking
                coEvery { revenueRepository.getRevenueForParkingOnDate(parkingId, today) } returns existingRevenue
                coEvery { revenueRepository.upsert(any()) } returns updatedRevenue

                // When
                val result =
                    handler.handle(
                        eventType = EventType.EXIT,
                        latitude = latitude,
                        longitude = longitude,
                        amountPaid = amountPaid,
                    )

                // Then
                result shouldBe updatedRevenue
            }

            it("should throw RevenueNotFoundException when revenue doesn't exist") {
                // Given
                coEvery { parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude) } returns parking
                coEvery { revenueRepository.getRevenueForParkingOnDate(parkingId, today) } returns null

                // When/Then
                shouldThrow<RevenueNotFoundException> {
                    handler.handle(
                        eventType = EventType.EXIT,
                        latitude = latitude,
                        longitude = longitude,
                        amountPaid = amountPaid,
                    )
                }
            }
        }

        context("when handling other event types") {
            it("should return null for ENTRY event") {
                // Given
                coEvery { parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude) } returns parking

                // When
                val result =
                    handler.handle(
                        eventType = EventType.ENTRY,
                        latitude = latitude,
                        longitude = longitude,
                    )

                // Then
                result shouldBe null
            }
        }

        it("should return null when parking id is null") {
            // Given
            val parkingWithoutId = parking.copy(id = null)
            coEvery {
                parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
            } returns parkingWithoutId

            // When
            val result =
                handler.handle(
                    eventType = EventType.PARKED,
                    latitude = latitude,
                    longitude = longitude,
                )

            // Then
            result shouldBe null
        }
    }
})
