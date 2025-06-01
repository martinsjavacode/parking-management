package io.github.martinsjavacode.parkingmanagement.adapters.extension.parking

import io.github.martinsjavacode.parkingmanagement.adapters.outbound.client.response.ParkingDataResponse
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEntity
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal
import java.time.LocalTime

class ParkingExtensionTest : DescribeSpec({
    describe("ParkingExtension") {
        describe("ParkingDataResponse.toDomain") {
            it("should convert ParkingDataResponse to Parking domain model") {
                // Given
                val parkingDataResponse =
                    ParkingDataResponse(
                        sector = "A",
                        basePrice = BigDecimal("10.00"),
                        maxCapacity = 100,
                        openHour = "08:00",
                        closeHour = "20:00",
                        durationLimitMinutes = 120,
                    )

                val parkingSpots =
                    flowOf(
                        ParkingSpot(
                            id = 1L,
                            parkingId = 1L,
                            latitude = 10.123456,
                            longitude = -20.654321,
                        ),
                    )

                // When
                val result = parkingDataResponse.toDomain(parkingSpots)

                // Then
                result.shouldBeInstanceOf<Parking>()
                result.sector shouldBe "A"
                result.basePrice shouldBe BigDecimal("10.00")
                result.maxCapacity shouldBe 100
                result.openHour shouldBe LocalTime.of(8, 0)
                result.closeHour shouldBe LocalTime.of(20, 0)
                result.durationLimitMinutes shouldBe 120
            }
        }

        describe("Parking.toEntity") {
            it("should convert Parking domain model to ParkingEntity") {
                // Given
                val parking =
                    Parking(
                        id = 1L,
                        sector = "B",
                        basePrice = BigDecimal("15.00"),
                        maxCapacity = 50,
                        openHour = LocalTime.of(9, 0),
                        closeHour = LocalTime.of(21, 0),
                        durationLimitMinutes = 180,
                        spots = flowOf(),
                    )

                // When
                val result = parking.toEntity()

                // Then
                result.shouldBeInstanceOf<ParkingEntity>()
                result.id shouldBe 1L
                result.sector shouldBe "B"
                result.basePrice shouldBe BigDecimal("15.00")
                result.maxCapacity shouldBe 50
                result.openHour shouldBe LocalTime.of(9, 0)
                result.closeHour shouldBe LocalTime.of(21, 0)
                result.durationLimitMinutes shouldBe 180
            }
        }

        describe("ParkingEntity.toDomain") {
            it("should convert ParkingEntity to Parking domain model") {
                // Given
                val parkingEntity =
                    ParkingEntity(
                        id = 2L,
                        sector = "C",
                        basePrice = BigDecimal("20.00"),
                        maxCapacity = 75,
                        openHour = LocalTime.of(7, 30),
                        closeHour = LocalTime.of(22, 30),
                        durationLimitMinutes = 240,
                    )

                // When
                val result = parkingEntity.toDomain()

                // Then
                result.shouldBeInstanceOf<Parking>()
                result.id shouldBe 2L
                result.sector shouldBe "C"
                result.basePrice shouldBe BigDecimal("20.00")
                result.maxCapacity shouldBe 75
                result.openHour shouldBe LocalTime.of(7, 30)
                result.closeHour shouldBe LocalTime.of(22, 30)
                result.durationLimitMinutes shouldBe 240
            }
        }
    }
})
