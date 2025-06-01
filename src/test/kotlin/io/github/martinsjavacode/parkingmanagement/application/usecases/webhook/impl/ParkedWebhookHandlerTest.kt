package io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.CalculatePricingMultiplierHandler
import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetParkingByCoordinatesOrThrowHandler
import io.github.martinsjavacode.parkingmanagement.application.usecases.revenue.UpdateOrInitializeDailyRevenueHandler
import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.EntryEventNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime

class ParkedWebhookHandlerTest : BehaviorSpec({
    val messageSource = mockk<MessageSource>(relaxed = true)
    val traceContext = mockk<TraceContext>(relaxed = true)
    val parkingEventRepository = mockk<ParkingEventRepositoryPort>()
    val getParkingByCoordinatesOrThrowHandler = mockk<GetParkingByCoordinatesOrThrowHandler>()
    val calculatePricingMultiplierHandler = mockk<CalculatePricingMultiplierHandler>()
    val updateOrInitializeDailyRevenueHandler = mockk<UpdateOrInitializeDailyRevenueHandler>()

    val parkedWebhookHandler = ParkedWebhookHandler(
        messageSource = messageSource,
        traceContext = traceContext,
        parkingEventRepository = parkingEventRepository,
        getParkingByCoordinatesOrThrowHandler = getParkingByCoordinatesOrThrowHandler,
        calculatePricingMultiplierHandler = calculatePricingMultiplierHandler,
        initializeDailyRevenueHandler = updateOrInitializeDailyRevenueHandler
    )

    beforeTest {
        clearAllMocks()

        // Default mocks for all tests
        coEvery { traceContext.traceId() } returns "test-trace-id"
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    given("A parked webhook event") {
        val now = LocalDateTime.now()
        val licensePlate = "ABC1234"
        val latitude = -23.561684
        val longitude = -46.655981
        val priceMultiplier = 1.1

        val validEvent = WebhookEvent(
            licensePlate = licensePlate,
            lat = latitude,
            lng = longitude,
            entryTime = null,
            exitTime = null,
            eventType = EventType.PARKED
        )

        val entryEvent = ParkingEvent(
            id = 1L,
            licensePlate = licensePlate,
            latitude = 0.0, // Initial coordinates are not important for entry event
            longitude = 0.0,
            entryTime = now.minusHours(1),
            exitTime = null,
            eventType = EventType.ENTRY,
            priceMultiplier = 1.0,
            amountPaid = BigDecimal.ZERO
        )

        val parking = Parking(
            id = 1L,
            sector = "A",
            basePrice = BigDecimal("10.00"),
            maxCapacity = 100,
            openHour = LocalTime.of(8, 0),
            closeHour = LocalTime.of(20, 0),
            durationLimitMinutes = 60,
            spots = emptyFlow()
        )

        val revenue = Revenue(
            id = 1L,
            parkingId = 1L,
            date = now.toLocalDate(),
            amount = BigDecimal.ZERO,
            currency = CurrencyType.BRL
        )

        `when`("The event has an invalid type") {
            val invalidEvent = validEvent.copy(eventType = EventType.ENTRY)

            then("Should throw IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    parkedWebhookHandler.handle(invalidEvent)
                }
            }
        }

        `when`("The event has invalid coordinates") {
            val invalidEvent = validEvent.copy(lat = 100.0) // Invalid latitude

            beforeTest {
                coEvery { getParkingByCoordinatesOrThrowHandler.handle(any(), any()) } returns parking
            }

            then("Should throw IllegalStateException") {
                shouldThrow<IllegalStateException> {
                    parkedWebhookHandler.handle(invalidEvent)
                }
            }
        }

        `when`("No parking is found for the coordinates") {
            beforeTest {
                coEvery {
                    getParkingByCoordinatesOrThrowHandler.handle(latitude, longitude)
                } throws IllegalStateException("No parking found")
            }

            then("Should throw IllegalStateException") {
                shouldThrow<IllegalStateException> {
                    parkedWebhookHandler.handle(validEvent)
                }
            }
        }

        `when`("No entry event is found for the license plate") {
            beforeTest {
                coEvery { getParkingByCoordinatesOrThrowHandler.handle(latitude, longitude) } returns parking
                coEvery { calculatePricingMultiplierHandler.handle(latitude, longitude) } returns priceMultiplier
                coEvery { parkingEventRepository.findAllByLicensePlate(licensePlate) } returns emptyFlow()
            }

            then("Should throw EntryEventNotFoundException") {
                shouldThrow<EntryEventNotFoundException> {
                    parkedWebhookHandler.handle(validEvent)
                }
            }
        }

        `when`("The event is valid and all dependencies work correctly") {
            val savedEvent = slot<ParkingEvent>()

            beforeTest {
                coEvery { getParkingByCoordinatesOrThrowHandler.handle(latitude, longitude) } returns parking
                coEvery { calculatePricingMultiplierHandler.handle(latitude, longitude) } returns priceMultiplier
                coEvery { parkingEventRepository.findAllByLicensePlate(licensePlate) } returns flowOf(entryEvent)
                coEvery { parkingEventRepository.save(capture(savedEvent)) } just Runs
                coEvery {
                    updateOrInitializeDailyRevenueHandler.handle(
                        eventType = EventType.PARKED,
                        latitude = latitude,
                        longitude = longitude,
                        amountPaid = any()
                    )
                } returns revenue
            }

            then("Should process the parked event successfully") {
                parkedWebhookHandler.handle(validEvent)

                // Verify repository calls
                coVerify(exactly = 1) { getParkingByCoordinatesOrThrowHandler.handle(latitude, longitude) }
                coVerify(exactly = 1) { calculatePricingMultiplierHandler.handle(latitude, longitude) }
                coVerify(exactly = 1) { parkingEventRepository.findAllByLicensePlate(licensePlate) }

                // Verify the event was saved correctly
                coVerify(exactly = 1) {
                    parkingEventRepository.save(match {
                        it.licensePlate == licensePlate &&
                        it.eventType == EventType.PARKED &&
                        it.latitude == latitude &&
                        it.longitude == longitude &&
                        it.priceMultiplier == priceMultiplier
                    })
                }

                // Verify revenue was initialized
                coVerify(exactly = 1) {
                    updateOrInitializeDailyRevenueHandler.handle(
                        eventType = EventType.PARKED,
                        latitude = latitude,
                        longitude = longitude,
                        amountPaid = any()
                    )
                }
            }
        }

        `when`("Multiple events exist for the license plate but only one is ENTRY") {
            val anotherParkedEvent = ParkingEvent(
                id = 2L,
                licensePlate = licensePlate,
                latitude = latitude,
                longitude = longitude,
                entryTime = now.minusHours(2),
                exitTime = null,
                eventType = EventType.PARKED,
                priceMultiplier = 1.0,
                amountPaid = BigDecimal.ZERO
            )

            beforeTest {
                coEvery { getParkingByCoordinatesOrThrowHandler.handle(latitude, longitude) } returns parking
                coEvery { calculatePricingMultiplierHandler.handle(latitude, longitude) } returns priceMultiplier
                coEvery { parkingEventRepository.findAllByLicensePlate(licensePlate) } returns flowOf(entryEvent, anotherParkedEvent)
                coEvery { parkingEventRepository.save(any()) } just Runs
                coEvery {
                    updateOrInitializeDailyRevenueHandler.handle(
                        eventType = EventType.PARKED,
                        latitude = latitude,
                        longitude = longitude,
                        amountPaid = any()
                    )
                } returns revenue
            }

            then("Should find and process the ENTRY event") {
                parkedWebhookHandler.handle(validEvent)

                coVerify(exactly = 1) { parkingEventRepository.findAllByLicensePlate(licensePlate) }
                coVerify(exactly = 1) {
                    parkingEventRepository.save(match {
                        it.id == 1L &&
                        it.eventType == EventType.PARKED
                    })
                }
            }
        }
    }
})
