package io.github.martinsjavacode.parkingmanagement.unit.adapters.extension.parking

import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toDomain
import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toEntity
import io.github.martinsjavacode.parkingmanagement.adapters.outbound.client.response.ParkingDataResponse
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEntity
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import java.math.BigDecimal
import java.time.LocalTime

class ParkingExtensionTest : DescribeSpec({

    describe("ParkingExtension") {

        context("ParkingDataResponse.toDomain") {
            it("should convert ParkingDataResponse to Parking domain model") {
                // Given
                val parkingDataResponse =
                    ParkingDataResponse(
                        sector = "A",
                        basePrice = BigDecimal("10.00"),
                        maxCapacity = 100,
                        openHour = "08:00",
                        closeHour = "20:00",
                        durationLimitMinutes = 60,
                    )

                val parkingSpots =
                    flowOf(
                        ParkingSpot(
                            id = null,
                            parkingId = 0,
                            latitude = 45.0,
                            longitude = 90.0,
                        ),
                    )

                // When
                val result = parkingDataResponse.toDomain(parkingSpots)

                // Then
                result.id shouldBe null
                result.sector shouldBe "A"
                result.basePrice shouldBe BigDecimal("10.00")
                result.maxCapacity shouldBe 100
                result.openHour shouldBe LocalTime.of(8, 0)
                result.closeHour shouldBe LocalTime.of(20, 0)
                result.durationLimitMinutes shouldBe 60
            }
        }

        context("Parking.toEntity") {
            it("should convert Parking domain model to ParkingEntity") {
                // Given
                val parking =
                    Parking(
                        id = 1L,
                        sector = "A",
                        basePrice = BigDecimal("10.00"),
                        maxCapacity = 100,
                        openHour = LocalTime.of(8, 0),
                        closeHour = LocalTime.of(20, 0),
                        durationLimitMinutes = 60,
                        spots = flowOf(),
                    )

                // When
                val result = parking.toEntity()

                // Then
                result.id shouldBe 1L
                result.sector shouldBe "A"
                result.basePrice shouldBe BigDecimal("10.00")
                result.maxCapacity shouldBe 100
                result.openHour shouldBe LocalTime.of(8, 0)
                result.closeHour shouldBe LocalTime.of(20, 0)
                result.durationLimitMinutes shouldBe 60
            }
        }

        context("ParkingEntity.toDomain") {
            it("should convert ParkingEntity to Parking domain model") {
                // Given
                val parkingEntity =
                    ParkingEntity(
                        id = 1L,
                        sector = "A",
                        basePrice = BigDecimal("10.00"),
                        maxCapacity = 100,
                        openHour = LocalTime.of(8, 0),
                        closeHour = LocalTime.of(20, 0),
                        durationLimitMinutes = 60,
                    )

                // When
                val result = parkingEntity.toDomain()

                // Then
                result.id shouldBe 1L
                result.sector shouldBe "A"
                result.basePrice shouldBe BigDecimal("10.00")
                result.maxCapacity shouldBe 100
                result.openHour shouldBe LocalTime.of(8, 0)
                result.closeHour shouldBe LocalTime.of(20, 0)
                result.durationLimitMinutes shouldBe 60

                // Verify spots is empty flow
                result.spots.toList() shouldBe emptyList()
            }
        }
    }
})
