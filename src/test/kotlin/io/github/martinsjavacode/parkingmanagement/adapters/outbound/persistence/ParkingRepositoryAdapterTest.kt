package io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence

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
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.LocalTime

class ParkingRepositoryAdapterTest : BehaviorSpec({
    val parkingRepository = mockk<ParkingRepository>()
    val parkingSpotRepository = mockk<ParkingSpotRepository>()
    val messageSource = mockk<MessageSource>(relaxed = true)
    val traceContext = mockk<TraceContext>(relaxed = true)

    val parkingRepositoryAdapter =
        ParkingRepositoryAdapter(
            parkingRepository = parkingRepository,
            parkingSpotRepository = parkingSpotRepository,
            messageSource = messageSource,
            traceContext = traceContext,
        )

    beforeTest {
        clearAllMocks()

        // Default mocks for all tests
        coEvery { traceContext.traceId() } returns "test-trace-id"
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    given("A parking entity") {
        val parkingId = 1L
        val sector = "A"
        val basePrice = BigDecimal("10.00")
        val maxCapacity = 100
        val openHour = LocalTime.of(8, 0)
        val closeHour = LocalTime.of(20, 0)
        val durationLimitMinutes = 60

        val parkingEntity =
            ParkingEntity(
                id = parkingId,
                sector = sector,
                basePrice = basePrice,
                maxCapacity = maxCapacity,
                openHour = openHour,
                closeHour = closeHour,
                durationLimitMinutes = durationLimitMinutes,
            )

        val parking =
            Parking(
                id = parkingId,
                sector = sector,
                basePrice = basePrice,
                maxCapacity = maxCapacity,
                openHour = openHour,
                closeHour = closeHour,
                durationLimitMinutes = durationLimitMinutes,
                spots = flowOf(),
            )

        `when`("Upserting a parking without spots") {
            beforeTest {
                coEvery { parkingRepository.save(any()) } returns parkingEntity
            }

            then("Should save the parking entity") {
                parkingRepositoryAdapter.upsert(parking)

                coVerify(exactly = 1) { parkingRepository.save(any()) }
                coVerify(exactly = 0) { parkingSpotRepository.save(any()) }
            }
        }

        `when`("Upserting a parking with spots") {
            val parkingSpot1 =
                ParkingSpot(
                    id = null,
                    parkingId = null,
                    latitude = 10.0,
                    longitude = 20.0,
                )

            val parkingSpot2 =
                ParkingSpot(
                    id = null,
                    parkingId = null,
                    latitude = 30.0,
                    longitude = 40.0,
                )

            val parkingWithSpots =
                parking.copy(
                    spots = flowOf(parkingSpot1, parkingSpot2),
                )

            val parkingSpotEntity1 =
                ParkingSpotEntity(
                    id = null,
                    parkingId = parkingId,
                    latitude = 10.0,
                    longitude = 20.0,
                )

            val parkingSpotEntity2 =
                ParkingSpotEntity(
                    id = null,
                    parkingId = parkingId,
                    latitude = 30.0,
                    longitude = 40.0,
                )

            beforeTest {
                coEvery { parkingRepository.save(any()) } returns parkingEntity
                coEvery { parkingSpotRepository.save(any()) } returnsMany
                    listOf(
                        parkingSpotEntity1.copy(id = 1L),
                        parkingSpotEntity2.copy(id = 2L),
                    )
            }

            then("Should save the parking entity and its spots") {
                parkingRepositoryAdapter.upsert(parkingWithSpots)

                coVerify(exactly = 1) { parkingRepository.save(any()) }
                coVerify(exactly = 2) { parkingSpotRepository.save(any()) }
            }
        }

        `when`("Upserting a parking fails") {
            beforeTest {
                coEvery { parkingRepository.save(any()) } throws RuntimeException("Database error")
            }

            then("Should throw ParkingSaveFailedException") {
                shouldThrow<ParkingSaveFailedException> {
                    parkingRepositoryAdapter.upsert(parking)
                }

                coVerify(exactly = 1) { parkingRepository.save(any()) }
                coVerify(exactly = 0) { parkingSpotRepository.save(any()) }
            }
        }

        `when`("Finding all parkings") {
            beforeTest {
                coEvery { parkingRepository.findAll() } returns flowOf(parkingEntity)
            }

            then("Should return a flow of parkings") {
                val result = parkingRepositoryAdapter.findAll().toList()

                result.size shouldBe 1
                result[0].id shouldBe parkingId
                result[0].sector shouldBe sector

                coVerify(exactly = 1) { parkingRepository.findAll() }
            }
        }

        `when`("Finding all parkings fails") {
            beforeTest {
                coEvery { parkingRepository.findAll() } throws RuntimeException("Database error")
            }

            then("Should throw ParkingNotFoundException") {
                shouldThrow<ParkingNotFoundException> {
                    parkingRepositoryAdapter.findAll().toList()
                }

                coVerify(exactly = 1) { parkingRepository.findAll() }
            }
        }

        `when`("Finding a parking by sector name") {
            beforeTest {
                coEvery { parkingRepository.findBySector(sector) } returns parkingEntity
            }

            then("Should return the parking") {
                val result = parkingRepositoryAdapter.findBySectorName(sector)

                result.id shouldBe parkingId
                result.sector shouldBe sector

                coVerify(exactly = 1) { parkingRepository.findBySector(sector) }
            }
        }

        `when`("Finding a parking by sector name fails") {
            beforeTest {
                coEvery { parkingRepository.findBySector(sector) } throws RuntimeException("Database error")
            }

            then("Should throw ParkingNotFoundException") {
                shouldThrow<ParkingNotFoundException> {
                    parkingRepositoryAdapter.findBySectorName(sector)
                }

                coVerify(exactly = 1) { parkingRepository.findBySector(sector) }
            }
        }
    }
})
