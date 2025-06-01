package io.github.martinsjavacode.parkingmanagement.adapters.extension.parking

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpotStatus
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingSpotEntity
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDateTime

class ParkingSpotExtensionTest : DescribeSpec({
    describe("ParkingSpotExtension") {
        describe("ParkingSpot.toEntity") {
            it("should convert ParkingSpot domain model to ParkingSpotEntity") {
                // Given
                val parkingSpot = ParkingSpot(
                    id = 1L,
                    parkingId = 10L,
                    latitude = 10.123456,
                    longitude = -20.654321
                )
                
                // When
                val result = parkingSpot.toEntity()
                
                // Then
                result.id shouldBe 1L
                result.parkingId shouldBe 10L
                result.latitude shouldBe 10.123456
                result.longitude shouldBe -20.654321
            }
            
            it("should handle null parkingId correctly") {
                // Given
                val parkingSpot = ParkingSpot(
                    id = 2L,
                    parkingId = null,
                    latitude = 15.654321,
                    longitude = -25.123456
                )
                
                // When
                val result = parkingSpot.toEntity()
                
                // Then
                result.id shouldBe 2L
                result.parkingId shouldBe 0L
                result.latitude shouldBe 15.654321
                result.longitude shouldBe -25.123456
            }
        }
        
        describe("ParkingSpotEntity.toDomain") {
            it("should convert ParkingSpotEntity to ParkingSpot domain model") {
                // Given
                val parkingSpotEntity = ParkingSpotEntity(
                    id = 3L,
                    parkingId = 20L,
                    latitude = 30.123456,
                    longitude = -40.654321
                )
                
                // When
                val result = parkingSpotEntity.toDomain()
                
                // Then
                result.id shouldBe 3L
                result.parkingId shouldBe 20L
                result.latitude shouldBe 30.123456
                result.longitude shouldBe -40.654321
            }
        }
        
        describe("ParkingSpotStatus.toResponse") {
            it("should convert ParkingSpotStatus to SpotStatusResponse when spot is occupied") {
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
                
                val parkingSpotStatus = ParkingSpotStatus(
                    occupied = true,
                    parkingEvent = parkingEvent,
                    priceUntilNow = BigDecimal("15.00"),
                    timeParked = now.plusHours(1)
                )
                
                // When
                val result = parkingSpotStatus.toResponse()
                
                // Then
                result.ocupied shouldBe true
                result.licensePlate shouldBe "ABC1234"
                result.priceUntilNow shouldBe BigDecimal("15.00")
                result.entryTime shouldBe now
                result.timeParked shouldBe now.plusHours(1)
            }
            
            it("should convert ParkingSpotStatus to SpotStatusResponse when spot is not occupied") {
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
                
                val parkingSpotStatus = ParkingSpotStatus(
                    occupied = false,
                    parkingEvent = parkingEvent,
                    priceUntilNow = BigDecimal("0.00"),
                    timeParked = now
                )
                
                // When
                val result = parkingSpotStatus.toResponse()
                
                // Then
                result.ocupied shouldBe false
                result.licensePlate shouldBe ""
                result.priceUntilNow shouldBe BigDecimal("0.00")
                result.entryTime shouldBe now
                result.timeParked shouldBe now
            }
        }
    }
})
