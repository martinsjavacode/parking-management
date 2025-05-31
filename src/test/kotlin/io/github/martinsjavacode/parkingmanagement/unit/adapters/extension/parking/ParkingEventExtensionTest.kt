package io.github.martinsjavacode.parkingmanagement.unit.adapters.extension.parking

import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toDomain
import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toEntity
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEventEntity
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDateTime

class ParkingEventExtensionTest : DescribeSpec({

    describe("ParkingEventExtension") {

        context("ParkingEvent.toEntity") {
            it("should convert ParkingEvent domain model to ParkingEventEntity") {
                // Given
                val now = LocalDateTime.now()
                val parkingEvent =
                    ParkingEvent(
                        id = 1L,
                        licensePlate = "ABC1234",
                        latitude = 45.0,
                        longitude = 90.0,
                        entryTime = now,
                        exitTime = now.plusHours(2),
                        eventType = EventType.EXIT,
                        priceMultiplier = 1.1,
                        amountPaid = BigDecimal("22.00"),
                    )

                // When
                val result = parkingEvent.toEntity()

                // Then
                result.id shouldBe 1L
                result.licensePlate shouldBe "ABC1234"
                result.latitude shouldBe 45.0
                result.longitude shouldBe 90.0
                result.entryTime shouldBe now
                result.exitTime shouldBe now.plusHours(2)
                result.eventType shouldBe EventType.EXIT
                result.priceMultiplier shouldBe 1.1
                result.amountPaid shouldBe BigDecimal("22.00")
            }

            it("should handle null values correctly") {
                // Given
                val now = LocalDateTime.now()
                val parkingEvent =
                    ParkingEvent(
                        id = null,
                        licensePlate = "ABC1234",
                        latitude = 45.0,
                        longitude = 90.0,
                        entryTime = now,
                        exitTime = null,
                        eventType = EventType.ENTRY,
                        priceMultiplier = 1.0,
                        amountPaid = BigDecimal.ZERO,
                    )

                // When
                val result = parkingEvent.toEntity()

                // Then
                result.id shouldBe null
                result.licensePlate shouldBe "ABC1234"
                result.latitude shouldBe 45.0
                result.longitude shouldBe 90.0
                result.entryTime shouldBe now
                result.exitTime shouldBe null
                result.eventType shouldBe EventType.ENTRY
                result.priceMultiplier shouldBe 1.0
                result.amountPaid shouldBe BigDecimal.ZERO
            }
        }

        context("ParkingEventEntity.toDomain") {
            it("should convert ParkingEventEntity to ParkingEvent domain model") {
                // Given
                val now = LocalDateTime.now()
                val parkingEventEntity =
                    ParkingEventEntity(
                        id = 1L,
                        licensePlate = "ABC1234",
                        latitude = 45.0,
                        longitude = 90.0,
                        entryTime = now,
                        exitTime = now.plusHours(2),
                        eventType = EventType.EXIT,
                        priceMultiplier = 1.1,
                        amountPaid = BigDecimal("22.00"),
                    )

                // When
                val result = parkingEventEntity.toDomain()

                // Then
                result.id shouldBe 1L
                result.licensePlate shouldBe "ABC1234"
                result.latitude shouldBe 45.0
                result.longitude shouldBe 90.0
                result.entryTime shouldBe now
                result.exitTime shouldBe now.plusHours(2)
                result.eventType shouldBe EventType.EXIT
                result.priceMultiplier shouldBe 1.1
                result.amountPaid shouldBe BigDecimal("22.00")
            }

            it("should handle null values correctly") {
                // Given
                val now = LocalDateTime.now()
                val parkingEventEntity =
                    ParkingEventEntity(
                        id = null,
                        licensePlate = "ABC1234",
                        latitude = 45.0,
                        longitude = 90.0,
                        entryTime = now,
                        exitTime = null,
                        eventType = EventType.ENTRY,
                        priceMultiplier = 1.0,
                        amountPaid = BigDecimal.ZERO,
                    )

                // When
                val result = parkingEventEntity.toDomain()

                // Then
                result.id shouldBe null
                result.licensePlate shouldBe "ABC1234"
                result.latitude shouldBe 45.0
                result.longitude shouldBe 90.0
                result.entryTime shouldBe now
                result.exitTime shouldBe null
                result.eventType shouldBe EventType.ENTRY
                result.priceMultiplier shouldBe 1.0
                result.amountPaid shouldBe BigDecimal.ZERO
            }
        }
    }
})
