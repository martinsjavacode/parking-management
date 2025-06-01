package io.github.martinsjavacode.parkingmanagement.application.usecases.plate.impl

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.flow.emptyFlow
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime

class GetPlateStatusHandlerImplTest : BehaviorSpec({
    val parkingEventRepository = mockk<ParkingEventRepositoryPort>()
    val parkingCustomQueryRepository = mockk<ParkingCustomQueryRepositoryPort>()

    val getPlateStatusHandler =
        GetPlateStatusHandlerImpl(
            parkingEventRepository = parkingEventRepository,
            parkingCustomQueryRepository = parkingCustomQueryRepository,
        )

    beforeTest {
        clearAllMocks()
    }

    given("A license plate") {
        val licensePlate = "ABC1234"
        val now = LocalDateTime.now()
        val entryTime = now.minusHours(2)
        val latitude = -23.561684
        val longitude = -46.655981
        val priceMultiplier = 1.1

        val parkingEvent =
            ParkingEvent(
                id = 1L,
                licensePlate = licensePlate,
                latitude = latitude,
                longitude = longitude,
                entryTime = entryTime,
                exitTime = null,
                eventType = EventType.PARKED,
                priceMultiplier = priceMultiplier,
                amountPaid = BigDecimal.ZERO,
            )

        val parking =
            Parking(
                id = 1L,
                sector = "A",
                basePrice = BigDecimal("10.00"),
                maxCapacity = 100,
                openHour = LocalTime.of(8, 0),
                closeHour = LocalTime.of(20, 0),
                durationLimitMinutes = 60,
                spots = emptyFlow(),
            )

        `when`("The plate has a PARKED event") {
            beforeTest {
                coEvery {
                    parkingEventRepository.findLastParkingEventByLicenseAndEventType(licensePlate, EventType.PARKED)
                } returns parkingEvent

                coEvery {
                    parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
                } returns parking
            }

            then("Should return the plate status with correct information") {
                val result = getPlateStatusHandler.handle(licensePlate)

                // Verify repository calls
                coVerify(exactly = 1) {
                    parkingEventRepository.findLastParkingEventByLicenseAndEventType(licensePlate, EventType.PARKED)
                }
                coVerify(exactly = 1) {
                    parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
                }

                // Verify result
                result shouldNotBe null
                result.parkingEvent shouldBe parkingEvent
                result.parking shouldBe parking
                result.priceUntilNow shouldNotBe BigDecimal.ZERO // Should have calculated a price
                result.timeParked shouldNotBe null // Should have calculated elapsed time
            }
        }

        `when`("The plate has no PARKED event but has an EXIT event") {
            val exitEvent =
                parkingEvent.copy(
                    eventType = EventType.EXIT,
                    exitTime = now,
                )

            beforeTest {
                coEvery {
                    parkingEventRepository.findLastParkingEventByLicenseAndEventType(licensePlate, EventType.PARKED)
                } throws RuntimeException("No PARKED event found")

                coEvery {
                    parkingEventRepository.findLastParkingEventByLicenseAndEventType(licensePlate, EventType.EXIT)
                } returns exitEvent

                coEvery {
                    parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
                } returns parking
            }

            then("Should fallback to EXIT event and return the plate status") {
                val result = getPlateStatusHandler.handle(licensePlate)

                // Verify repository calls
                coVerify(exactly = 1) {
                    parkingEventRepository.findLastParkingEventByLicenseAndEventType(licensePlate, EventType.PARKED)
                }
                coVerify(exactly = 1) {
                    parkingEventRepository.findLastParkingEventByLicenseAndEventType(licensePlate, EventType.EXIT)
                }
                coVerify(exactly = 1) {
                    parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)
                }

                // Verify result
                result shouldNotBe null
                result.parkingEvent shouldBe exitEvent
                result.parking shouldBe parking
                result.priceUntilNow shouldNotBe BigDecimal.ZERO
                result.timeParked shouldNotBe null
            }
        }
    }
})
