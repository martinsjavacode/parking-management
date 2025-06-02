package io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.CalculatePricingMultiplierHandler
import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetMostRecentParkingEvent
import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetParkingByCoordinatesOrThrowHandler
import io.github.martinsjavacode.parkingmanagement.application.usecases.revenue.UpdateOrInitializeDailyRevenueHandler
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.redis.DistributedLockPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.DuplicateEventException
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingSpotOccupiedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import org.springframework.context.MessageSource
import java.time.LocalDateTime

class ParkedWebhookHandlerTest : BehaviorSpec({
    val messageSource = mockk<MessageSource>()
    val traceContext = mockk<TraceContext>()
    val parkingEventRepository = mockk<ParkingEventRepositoryPort>()
    val getParkingByCoordinatesOrThrowHandler = mockk<GetParkingByCoordinatesOrThrowHandler>()
    val calculatePricingMultiplierHandler = mockk<CalculatePricingMultiplierHandler>()
    val initializeDailyRevenueHandler = mockk<UpdateOrInitializeDailyRevenueHandler>()
    val getMostRecentParkingEvent = mockk<GetMostRecentParkingEvent>()
    val distributedLock = mockk<DistributedLockPort>()

    val handler =
        ParkedWebhookHandler(
            messageSource = messageSource,
            traceContext = traceContext,
            parkingEventRepository = parkingEventRepository,
            getParkingByCoordinatesOrThrowHandler = getParkingByCoordinatesOrThrowHandler,
            calculatePricingMultiplierHandler = calculatePricingMultiplierHandler,
            initializeDailyRevenueHandler = initializeDailyRevenueHandler,
            getMostRecentParkingEvent = getMostRecentParkingEvent,
            distributedLock = distributedLock,
        )

    Given("A parked webhook event") {
        val event =
            WebhookEvent(
                id = "test-event-id",
                licensePlate = "ABC1234",
                lat = 10.0,
                lng = 20.0,
                eventType = EventType.PARKED,
                entryTime = LocalDateTime.now(),
                exitTime = null,
            )

        coEvery { getParkingByCoordinatesOrThrowHandler.handle(any(), any()) } returns mockk()

        // Mock para o GetMostRecentParkingEvent
        val mockParkingEvent =
            ParkingEvent(
                id = 1L,
                licensePlate = "XYZ9876",
                latitude = 10.0,
                longitude = 20.0,
                eventType = EventType.EXIT,
                entryTime = LocalDateTime.now().minusHours(1),
            )
        coEvery { getMostRecentParkingEvent.handle(any(), any()) } returns mockParkingEvent

        coEvery { calculatePricingMultiplierHandler.handle(any(), any()) } returns 1.0
        coEvery { initializeDailyRevenueHandler.handle(any(), any(), any()) } returns mockk()
        coEvery { traceContext.traceId() } returns "trace-123"
        coEvery { messageSource.getMessage(any(), any(), any()) } returns "Error message"
        coEvery { parkingEventRepository.save(any()) } returns mockk()

        When("The event is a duplicate") {
            coEvery { distributedLock.checkAndMarkIdempotency(any(), any(), any()) } returns false

            Then("It should skip processing") {
                shouldThrow<DuplicateEventException> {
                    handler.handle(event)
                }
                coVerify(exactly = 0) { distributedLock.acquireLock(any(), any(), any()) }
            }
        }

        When("The event is new but lock cannot be acquired") {
            coEvery { distributedLock.checkAndMarkIdempotency(any(), any(), any()) } returns true
            coEvery { distributedLock.acquireLock(any(), any(), any()) } returns false

            Then("It should throw ParkingSpotOccupiedException") {
                shouldThrow<ParkingSpotOccupiedException> {
                    handler.handle(event)
                }
            }
        }

        When("The event is new and lock can be acquired") {
            val entryEvent =
                ParkingEvent(
                    id = 1L,
                    licensePlate = "ABC1234",
                    latitude = 9.0,
                    longitude = 19.0,
                    eventType = EventType.ENTRY,
                    entryTime = LocalDateTime.now().minusMinutes(10),
                    priceMultiplier = 1.0,
                )

            coEvery { distributedLock.checkAndMarkIdempotency(any(), any(), any()) } returns true
            coEvery { distributedLock.acquireLock(any(), any(), any()) } returns true
            coEvery { distributedLock.releaseLock(any(), any(), any()) } returns true
            coEvery { parkingEventRepository.findAllByLicensePlate(any()) } returns flowOf(entryEvent)

            Then("It should process the event and release the lock") {
                handler.handle(event)

                coVerify {
                    parkingEventRepository.save(
                        match<ParkingEvent> {
                            it.eventType == EventType.PARKED &&
                                it.licensePlate == "ABC1234" &&
                                it.latitude == 10.0 &&
                                it.longitude == 20.0
                        },
                    )
                }

                coVerify { distributedLock.releaseLock(10.0, 20.0, "ABC1234") }
            }
        }

        When("An exception occurs during processing") {
            coEvery { distributedLock.checkAndMarkIdempotency(any(), any(), any()) } returns true
            coEvery { distributedLock.acquireLock(any(), any(), any()) } returns true
            coEvery { distributedLock.releaseLock(any(), any(), any()) } returns true
            coEvery { parkingEventRepository.findAllByLicensePlate(any()) } throws RuntimeException("Test exception")

            Then("It should release the lock even if an exception occurs") {
                runCatching { handler.handle(event) }

                coVerify { distributedLock.releaseLock(10.0, 20.0, "ABC1234") }
            }
        }
    }
})
