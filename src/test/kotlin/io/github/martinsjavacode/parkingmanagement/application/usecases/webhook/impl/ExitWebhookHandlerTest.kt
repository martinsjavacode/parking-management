package io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.impl

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
import io.github.martinsjavacode.parkingmanagement.infra.exception.NoParkedEventFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime

class ExitWebhookHandlerTest : BehaviorSpec({
    val messageSource = mockk<MessageSource>(relaxed = true)
    val traceContext = mockk<TraceContext>(relaxed = true)
    val parkingEventRepository = mockk<ParkingEventRepositoryPort>()
    val getParkingByCoordinatesOrThrowHandler = mockk<GetParkingByCoordinatesOrThrowHandler>()
    val updateDailyRevenueHandler = mockk<UpdateOrInitializeDailyRevenueHandler>()

    val exitWebhookHandler =
        ExitWebhookHandler(
            messageSource = messageSource,
            traceContext = traceContext,
            parkingEventRepository = parkingEventRepository,
            getParkingByCoordinatesOrThrowHandler = getParkingByCoordinatesOrThrowHandler,
            updateDailyRevenueHandler = updateDailyRevenueHandler,
        )

    beforeTest {
        // Default mocks for all tests
        coEvery { traceContext.traceId() } returns "test-trace-id"
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    afterTest {
        clearAllMocks()
    }

    given("An exit webhook event") {
        val now = LocalDateTime.now()
        val exitTime = now.plusHours(2)
        val licensePlate = "ABC1234"
        val latitude = -23.561684
        val longitude = -46.655981
        val priceMultiplier = 1.1

        val validEvent =
            WebhookEvent(
                licensePlate = licensePlate,
                lat = null,
                lng = null,
                entryTime = null,
                exitTime = exitTime,
                eventType = EventType.EXIT,
            )

        val parkedEvent =
            ParkingEvent(
                id = 1L,
                licensePlate = licensePlate,
                latitude = latitude,
                longitude = longitude,
                entryTime = now,
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

        val revenue =
            Revenue(
                id = 1L,
                parkingId = 1L,
                date = now.toLocalDate(),
                amount = BigDecimal("10.00"),
                currency = CurrencyType.BRL,
            )

        `when`("The event has an invalid type") {
            val invalidEvent = validEvent.copy(eventType = EventType.PARKED)

            then("Should throw IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    exitWebhookHandler.handle(invalidEvent)
                }
            }
        }

        `when`("The event is missing exit time") {
            val invalidEvent = validEvent.copy(exitTime = null)

            then("Should throw IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    exitWebhookHandler.handle(invalidEvent)
                }
            }
        }

        `when`("No parked event is found for the license plate") {
            beforeTest {
                coEvery { parkingEventRepository.findAllByLicensePlate(licensePlate) } returns emptyFlow()
            }

            then("Should throw NoParkedEventFoundException") {
                shouldThrow<NoParkedEventFoundException> {
                    exitWebhookHandler.handle(validEvent)
                }
            }
        }

        `when`("A parked event is found but no parking is found for the coordinates") {
            beforeTest {
                coEvery { parkingEventRepository.findAllByLicensePlate(licensePlate) } returns flowOf(parkedEvent)
                coEvery {
                    getParkingByCoordinatesOrThrowHandler.handle(latitude, longitude)
                } throws IllegalStateException("No parking found")
            }

            then("Should throw IllegalStateException") {
                shouldThrow<IllegalStateException> {
                    exitWebhookHandler.handle(validEvent)
                }
            }
        }

        `when`("The event is valid and all dependencies work correctly") {
            val updatedEvent = slot<ParkingEvent>()

            beforeTest {
                coEvery { parkingEventRepository.findAllByLicensePlate(licensePlate) } returns flowOf(parkedEvent)
                coEvery { getParkingByCoordinatesOrThrowHandler.handle(latitude, longitude) } returns parking
                coEvery { parkingEventRepository.save(capture(updatedEvent)) } just Runs
                coEvery {
                    updateDailyRevenueHandler.handle(
                        eventType = EventType.EXIT,
                        latitude = latitude,
                        longitude = longitude,
                        amountPaid = any(),
                    )
                } returns revenue
            }

            then("Should process the exit event successfully") {
                exitWebhookHandler.handle(validEvent)

                // Verify repository calls
                coVerify(exactly = 1) { parkingEventRepository.findAllByLicensePlate(licensePlate) }
                coVerify(exactly = 1) { getParkingByCoordinatesOrThrowHandler.handle(latitude, longitude) }

                // Verify the event was updated correctly
                coVerify(exactly = 1) {
                    parkingEventRepository.save(
                        match {
                            it.licensePlate == licensePlate &&
                                it.eventType == EventType.EXIT &&
                                it.exitTime == exitTime &&
                                it.amountPaid > BigDecimal.ZERO
                        },
                    )
                }

                // Verify revenue was updated
                coVerify(exactly = 1) {
                    updateDailyRevenueHandler.handle(
                        eventType = EventType.EXIT,
                        latitude = latitude,
                        longitude = longitude,
                        amountPaid = any(),
                    )
                }
            }
        }

        `when`("Multiple events exist for the license plate but only one is PARKED") {
            val entryEvent =
                ParkingEvent(
                    id = 2L,
                    licensePlate = licensePlate,
                    latitude = latitude,
                    longitude = longitude,
                    entryTime = now.minusHours(1),
                    exitTime = null,
                    eventType = EventType.ENTRY,
                    priceMultiplier = 1.0,
                    amountPaid = BigDecimal.ZERO,
                )

            beforeTest {
                coEvery {
                    parkingEventRepository.findAllByLicensePlate(licensePlate)
                } returns flowOf(entryEvent, parkedEvent)
                coEvery { getParkingByCoordinatesOrThrowHandler.handle(latitude, longitude) } returns parking
                coEvery { parkingEventRepository.save(any()) } just Runs
                coEvery {
                    updateDailyRevenueHandler.handle(
                        eventType = EventType.EXIT,
                        latitude = latitude,
                        longitude = longitude,
                        amountPaid = any(),
                    )
                } returns revenue
            }

            then("Should find and process the PARKED event") {
                exitWebhookHandler.handle(validEvent)

                coVerify(exactly = 1) { parkingEventRepository.findAllByLicensePlate(licensePlate) }
                coVerify(exactly = 1) {
                    parkingEventRepository.save(
                        match {
                            it.id == 1L && // Should match the parkedEvent id
                                it.eventType == EventType.EXIT
                        },
                    )
                }
            }
        }
    }
})
