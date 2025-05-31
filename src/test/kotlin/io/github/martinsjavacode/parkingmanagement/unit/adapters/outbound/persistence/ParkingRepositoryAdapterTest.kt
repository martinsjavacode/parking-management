package io.github.martinsjavacode.parkingmanagement.unit.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence.ParkingRepositoryAdapter
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingSaveFailedException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEntity
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingSpotEntity
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingRepository
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingSpotRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.LocalTime

class ParkingRepositoryAdapterTest : DescribeSpec({

    val parkingRepository = mockk<ParkingRepository>()
    val parkingSpotRepository = mockk<ParkingSpotRepository>()
    val messageSource = mockk<MessageSource>()
    val traceContext = mockk<TraceContext>()

    val adapter =
        ParkingRepositoryAdapter(
            parkingRepository = parkingRepository,
            parkingSpotRepository = parkingSpotRepository,
            messageSource = messageSource,
            traceContext = traceContext,
        )

    beforeTest {
        clearAllMocks()

        // Common mocks
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
        every { traceContext.traceId() } returns "test-trace-id"
    }

    describe("ParkingRepositoryAdapter") {

        context("upsert") {
            it("should save parking and its spots successfully") {
                // Given
                val parkingSpot =
                    ParkingSpot(
                        id = null,
                        parkingId = 1L,
                        latitude = 45.0,
                        longitude = 90.0,
                    )

                val parking =
                    Parking(
                        id = null,
                        sector = "A",
                        basePrice = BigDecimal("10.00"),
                        maxCapacity = 100,
                        openHour = LocalTime.of(8, 0),
                        closeHour = LocalTime.of(20, 0),
                        durationLimitMinutes = 60,
                        spots = flowOf(parkingSpot),
                    )

                val savedParkingEntity =
                    ParkingEntity(
                        id = 1L,
                        sector = "A",
                        basePrice = BigDecimal("10.00"),
                        maxCapacity = 100,
                        openHour = LocalTime.of(8, 0),
                        closeHour = LocalTime.of(20, 0),
                        durationLimitMinutes = 60,
                    )

                coEvery { parkingRepository.save(any()) } returns savedParkingEntity
                coEvery { parkingSpotRepository.save(any()) } returns
                    ParkingSpotEntity(
                        id = 1L,
                        parkingId = 1L,
                        latitude = 45.0,
                        longitude = 90.0,
                    )

                // When
                adapter.upsert(parking)

                // Then
                coVerify(exactly = 1) { parkingRepository.save(any()) }
                coVerify(exactly = 1) { parkingSpotRepository.save(any()) }
            }

            it("should throw ParkingSaveFailedException when saving fails") {
                // Given
                val parking =
                    Parking(
                        id = null,
                        sector = "A",
                        basePrice = BigDecimal("10.00"),
                        maxCapacity = 100,
                        openHour = LocalTime.of(8, 0),
                        closeHour = LocalTime.of(20, 0),
                        durationLimitMinutes = 60,
                        spots = emptyFlow(),
                    )

                coEvery { parkingRepository.save(any()) } throws RuntimeException("Database error")

                // When/Then
                shouldThrow<ParkingSaveFailedException> {
                    adapter.upsert(parking)
                }

                coVerify(exactly = 1) { parkingRepository.save(any()) }
                coVerify(exactly = 0) { parkingSpotRepository.save(any()) }
            }
        }

        context("findAll") {
            it("should return all parkings") {
                // Given
                val parkingEntity1 =
                    ParkingEntity(
                        id = 1L,
                        sector = "A",
                        basePrice = BigDecimal("10.00"),
                        maxCapacity = 100,
                        openHour = LocalTime.of(8, 0),
                        closeHour = LocalTime.of(20, 0),
                        durationLimitMinutes = 60,
                    )

                val parkingEntity2 =
                    ParkingEntity(
                        id = 2L,
                        sector = "B",
                        basePrice = BigDecimal("15.00"),
                        maxCapacity = 50,
                        openHour = LocalTime.of(9, 0),
                        closeHour = LocalTime.of(21, 0),
                        durationLimitMinutes = 120,
                    )

                coEvery { parkingRepository.findAll() } returns flowOf(parkingEntity1, parkingEntity2)

                // When
                val result: Flow<Parking> = adapter.findAll()
                val resultList = result.toList()

                // Then
                resultList.size shouldBe 2
                resultList[0].id shouldBe 1L
                resultList[0].sector shouldBe "A"
                resultList[1].id shouldBe 2L
                resultList[1].sector shouldBe "B"

                coVerify(exactly = 1) { parkingRepository.findAll() }
            }

            it("should throw ParkingNotFoundException when finding all fails") {
                // Given
                coEvery { parkingRepository.findAll() } throws RuntimeException("Database error")

                // When/Then
                shouldThrow<ParkingNotFoundException> {
                    adapter.findAll()
                }

                coVerify(exactly = 1) { parkingRepository.findAll() }
            }
        }

        context("findBySectorName") {
            it("should return parking by sector name") {
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

                coEvery { parkingRepository.findBySector("A") } returns parkingEntity

                // When
                val result = adapter.findBySectorName("A")

                // Then
                result.id shouldBe 1L
                result.sector shouldBe "A"

                coVerify(exactly = 1) { parkingRepository.findBySector("A") }
            }

            it("should throw ParkingNotFoundException when finding by sector fails") {
                // Given
                coEvery { parkingRepository.findBySector("A") } throws RuntimeException("Database error")

                // When/Then
                shouldThrow<ParkingNotFoundException> {
                    adapter.findBySectorName("A")
                }

                coVerify(exactly = 1) { parkingRepository.findBySector("A") }
            }
        }
    }
})
