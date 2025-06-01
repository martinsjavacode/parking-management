package io.github.martinsjavacode.parkingmanagement.adapters.extension.parking

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEventEntity
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.math.BigDecimal
import java.time.LocalDateTime

class ParkingEventExtensionTest : DescribeSpec({
    describe("ParkingEventExtension") {
        describe("ParkingEvent.toEntity") {
            it("should convert ParkingEvent domain model to ParkingEventEntity") {
                // Given
                val now = LocalDateTime.now()
                val exitTime = now.plusHours(2)
                val parkingEvent =
                    ParkingEvent(
                        id = 1L,
                        licensePlate = "ABC1234",
                        entryTime = now,
                        eventType = EventType.ENTRY,
                        latitude = 10.123456,
                        longitude = -20.654321,
                        exitTime = exitTime,
                        priceMultiplier = 1.25,
                        amountPaid = BigDecimal("25.00"),
                    )

                // When
                val result = parkingEvent.toEntity()

                // Then
                result.id shouldBe 1L
                result.licensePlate shouldBe "ABC1234"
                result.entryTime shouldBe now
                result.eventType shouldBe EventType.ENTRY
                result.latitude shouldBe 10.123456
                result.longitude shouldBe -20.654321
                result.exitTime shouldBe exitTime
                result.priceMultiplier shouldBe 1.25
                result.amountPaid shouldBe BigDecimal("25.00")
            }
        }

        describe("ParkingEventEntity.toDomain") {
            it("should convert ParkingEventEntity to ParkingEvent domain model") {
                // Given
                val now = LocalDateTime.now()
                val exitTime = now.plusHours(3)
                val parkingEventEntity =
                    ParkingEventEntity(
                        id = 2L,
                        licensePlate = "XYZ9876",
                        entryTime = now,
                        eventType = EventType.EXIT,
                        latitude = 15.654321,
                        longitude = -25.123456,
                        exitTime = exitTime,
                        priceMultiplier = 0.9,
                        amountPaid = BigDecimal("18.00"),
                    )

                // When
                val result = parkingEventEntity.toDomain()

                // Then
                result.shouldBeInstanceOf<ParkingEvent>()
                result.id shouldBe 2L
                result.licensePlate shouldBe "XYZ9876"
                result.entryTime shouldBe now
                result.eventType shouldBe EventType.EXIT
                result.latitude shouldBe 15.654321
                result.longitude shouldBe -25.123456
                result.exitTime shouldBe exitTime
                result.priceMultiplier shouldBe 0.9
                result.amountPaid shouldBe BigDecimal("18.00")
            }
        }
    }
})
