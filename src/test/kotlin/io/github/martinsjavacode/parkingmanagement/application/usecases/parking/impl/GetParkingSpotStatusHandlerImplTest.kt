package io.github.martinsjavacode.parkingmanagement.application.usecases.parking.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetMostRecentParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import java.math.BigDecimal
import java.time.LocalDateTime

class GetParkingSpotStatusHandlerImplTest : DescribeSpec({

    val parkingCustomQueryRepository = mockk<ParkingCustomQueryRepositoryPort>()
    val getMostRecentParkingEvent = mockk<GetMostRecentParkingEvent>()

    val handler =
        GetParkingSpotStatusHandlerImpl(
            getMostRecentParkingEvent = getMostRecentParkingEvent,
            parkingCustomQueryRepository = parkingCustomQueryRepository,
        )

    describe("GetParkingSpotStatusHandler") {
        val latitude = 10.123456
        val longitude = -20.654321

        context("When getting spot status") {
            val parking = mockk<Parking>()
            coEvery { parking.basePrice } returns BigDecimal("5.00")
            coEvery { parking.durationLimitMinutes } returns 120

            coEvery { parkingCustomQueryRepository.findParkingByCoordinates(any(), any()) } returns parking

            it("should return the correct status when spot is occupied") {
                val parkedEvent =
                    ParkingEvent(
                        id = 1L,
                        licensePlate = "ABC1234",
                        latitude = latitude,
                        longitude = longitude,
                        eventType = EventType.PARKED,
                        entryTime = LocalDateTime.now().minusHours(1),
                    )

                coEvery { getMostRecentParkingEvent.handle(any(), any()) } returns parkedEvent

                val result = handler.handle(latitude, longitude)

                result.occupied shouldBe true
                result.parkingEvent.licensePlate shouldBe "ABC1234"
            }

            it("should return the correct status when last event is EXIT") {
                val exitEvent =
                    ParkingEvent(
                        id = 1L,
                        licensePlate = "ABC1234",
                        latitude = latitude,
                        longitude = longitude,
                        eventType = EventType.EXIT,
                        entryTime = LocalDateTime.now().minusHours(2),
                        exitTime = LocalDateTime.now().minusHours(1),
                    )

                coEvery { getMostRecentParkingEvent.handle(any(), any()) } returns exitEvent

                val result = handler.handle(latitude, longitude)

                result.occupied shouldBe false
                result.parkingEvent.licensePlate shouldBe "ABC1234"
            }
        }
    }
})
