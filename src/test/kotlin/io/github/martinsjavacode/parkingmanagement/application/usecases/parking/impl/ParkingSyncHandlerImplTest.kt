package io.github.martinsjavacode.parkingmanagement.application.usecases.parking.impl

import io.github.martinsjavacode.parkingmanagement.domain.gateway.client.ExternalParkingApiPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingSpotRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpot
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal
import java.time.LocalTime

class ParkingSyncHandlerImplTest : DescribeSpec({

    val externalParkingApi = mockk<ExternalParkingApiPort>()
    val parkingRepository = mockk<ParkingRepositoryPort>(relaxed = true)
    val parkingSpotRepository = mockk<ParkingSpotRepositoryPort>(relaxed = true)

    val handler =
        ParkingSyncHandlerImpl(
            externalParkingApi = externalParkingApi,
            parkingRepository = parkingRepository,
            parkingSpotRepository = parkingSpotRepository,
        )

    describe("ParkingSyncHandler") {
        context("when syncing parking data") {
            val spot1 =
                ParkingSpot(
                    id = 1L,
                    parkingId = 1L,
                    latitude = 10.123456,
                    longitude = -20.654321,
                )

            val spot2 =
                ParkingSpot(
                    id = 2L,
                    parkingId = 1L,
                    latitude = 10.123457,
                    longitude = -20.654322,
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
                    spots = flowOf(spot1, spot2),
                )

            it("should update existing parking and add new spots") {
                // Given
                coEvery { externalParkingApi.fetchGarageConfig() } returns flowOf(parking)
                coEvery { parkingRepository.findBySectorName("A") } returns parking
                coEvery { parkingSpotRepository.findByCoordinates(spot1.latitude, spot1.longitude) } returns spot1
                coEvery {
                    parkingSpotRepository.findByCoordinates(spot2.latitude, spot2.longitude)
                } throws RuntimeException("Spot not found")

                // When
                handler.handle()

                // Then
                coVerify(exactly = 1) { parkingRepository.findBySectorName("A") }
                coVerify(exactly = 1) { parkingSpotRepository.findByCoordinates(spot1.latitude, spot1.longitude) }
                coVerify(exactly = 1) { parkingSpotRepository.findByCoordinates(spot2.latitude, spot2.longitude) }
                coVerify(exactly = 1) { parkingSpotRepository.save(any()) }
            }

            it("should create new parking when it doesn't exist") {
                // Given
                val newParking = parking.copy(sector = "B")
                coEvery { externalParkingApi.fetchGarageConfig() } returns flowOf(newParking)
                coEvery { parkingRepository.findBySectorName("B") } throws RuntimeException("Parking not found")

                // When
                handler.handle()

                // Then
                coVerify(exactly = 1) { parkingRepository.findBySectorName("B") }
                coVerify(exactly = 1) { parkingRepository.upsert(newParking) }
            }
        }
    }
})
