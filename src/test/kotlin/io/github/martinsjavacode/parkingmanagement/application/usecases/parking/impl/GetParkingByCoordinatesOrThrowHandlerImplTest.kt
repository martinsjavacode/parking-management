package io.github.martinsjavacode.parkingmanagement.application.usecases.parking.impl

import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal
import java.time.LocalTime

class GetParkingByCoordinatesOrThrowHandlerImplTest : DescribeSpec({

    val parkingCustomQueryRepository = mockk<ParkingCustomQueryRepositoryPort>()
    val handler = GetParkingByCoordinatesOrThrowHandlerImpl(parkingCustomQueryRepository)

    describe("GetParkingByCoordinatesOrThrowHandler") {
        val latitude = 10.123456
        val longitude = -20.654321

        it("should return parking when found by coordinates") {
            // Given
            val expectedParking =
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
                parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
            } returns expectedParking

            // When
            val result = handler.handle(latitude, longitude)

            // Then
            result shouldBe expectedParking
        }
    }
})
