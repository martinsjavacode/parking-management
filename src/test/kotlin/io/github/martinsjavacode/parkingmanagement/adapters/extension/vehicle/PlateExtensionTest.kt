package io.github.martinsjavacode.parkingmanagement.adapters.extension.vehicle

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.vehicle.PlateStatus
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime

class PlateExtensionTest : DescribeSpec({
    describe("PlateExtension") {
        describe("PlateStatus.toResponse") {
            it("should convert PlateStatus to PlateStatusResponse") {
                // Given
                val now = LocalDateTime.now()
                val parkingEvent = ParkingEvent(
                    id = 1L,
                    licensePlate = "ABC1234",
                    entryTime = now,
                    eventType = EventType.PARKED,
                    latitude = 10.123456,
                    longitude = -20.654321,
                    exitTime = null,
                    priceMultiplier = 1.0,
                    amountPaid = BigDecimal.ZERO
                )
                
                val parking = Parking(
                    id = 1L,
                    sector = "A",
                    basePrice = BigDecimal("10.00"),
                    maxCapacity = 100,
                    openHour = LocalTime.of(8, 0),
                    closeHour = LocalTime.of(20, 0),
                    durationLimitMinutes = 120,
                    spots = flowOf()
                )
                
                val plateStatus = PlateStatus(
                    parkingEvent = parkingEvent,
                    parking = parking,
                    priceUntilNow = BigDecimal("15.00"),
                    timeParked = now.plusHours(1)
                )
                
                // When
                val result = plateStatus.toResponse()
                
                // Then
                result.licensePlate shouldBe "ABC1234"
                result.priceUntilNow shouldBe BigDecimal("15.00")
                result.entryTime shouldBe now
                result.timeParked shouldBe now.plusHours(1)
                result.lat shouldBe 10.123456
                result.lng shouldBe -20.654321
            }
        }
    }
})
