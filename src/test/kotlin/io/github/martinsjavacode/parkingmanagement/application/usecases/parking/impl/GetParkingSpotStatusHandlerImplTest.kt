package io.github.martinsjavacode.parkingmanagement.application.usecases.parking.impl

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime

class GetParkingSpotStatusHandlerImplTest : DescribeSpec({

    val parkingEventRepository = mockk<ParkingEventRepositoryPort>()
    val parkingCustomQueryRepository = mockk<ParkingCustomQueryRepositoryPort>()

    val handler =
        GetParkingSpotStatusHandlerImpl(
            parkingEventRepository = parkingEventRepository,
            parkingCustomQueryRepository = parkingCustomQueryRepository,
        )

    describe("GetParkingSpotStatusHandler") {
        val latitude = 10.123456
        val longitude = -20.654321
        val now = LocalDateTime.now()

        context("when spot is occupied (PARKED event)") {
            // Given
            val parkingEvent =
                ParkingEvent(
                    id = 1L,
                    licensePlate = "ABC1234",
                    latitude = latitude,
                    longitude = longitude,
                    entryTime = now.minusHours(2),
                    exitTime = null,
                    eventType = EventType.PARKED,
                    priceMultiplier = 1.0,
                    amountPaid = BigDecimal.ZERO,
                )

            val parking =
                Parking(
                    id = 1L,
                    sector = "A",
                    basePrice = BigDecimal("10.00"),
                    maxCapacity = 100,
                    openHour = LocalTime.of(8, 0),
                    closeHour = LocalTime.of(20, 0),
                    durationLimitMinutes = 120,
                    spots = flowOf(),
                )

            coEvery {
                parkingEventRepository.findMostRecentByCoordinates(latitude, longitude)
            } returns parkingEvent

            coEvery {
                parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
            } returns parking

            it("should return occupied status with calculated price") {
                // When
                val result = handler.handle(latitude, longitude)

                // Then
                result.occupied shouldBe true
                result.parkingEvent shouldBe parkingEvent
                result.priceUntilNow.compareTo(BigDecimal.ZERO) shouldBe 1 // Price should be greater than zero
            }
        }

        context("when spot is not occupied (EXIT event)") {
            // Given
            val exitTime = now.minusHours(1)
            val parkingEvent =
                ParkingEvent(
                    id = 1L,
                    licensePlate = "ABC1234",
                    latitude = latitude,
                    longitude = longitude,
                    entryTime = now.minusHours(3),
                    exitTime = exitTime,
                    eventType = EventType.EXIT,
                    priceMultiplier = 1.0,
                    amountPaid = BigDecimal("25.00"),
                )

            val parking =
                Parking(
                    id = 1L,
                    sector = "A",
                    basePrice = BigDecimal("10.00"),
                    maxCapacity = 100,
                    openHour = LocalTime.of(8, 0),
                    closeHour = LocalTime.of(20, 0),
                    durationLimitMinutes = 120,
                    spots = flowOf(),
                )

            coEvery {
                parkingEventRepository.findMostRecentByCoordinates(latitude, longitude)
            } returns parkingEvent

            coEvery {
                parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
            } returns parking

            it("should return not occupied status with paid amount") {
                // When
                val result = handler.handle(latitude, longitude)

                // Then
                result.occupied shouldBe false
                result.parkingEvent shouldBe parkingEvent
                result.priceUntilNow shouldBe BigDecimal("25.00")
                result.timeParked shouldBe exitTime
            }
        }
    }
})
