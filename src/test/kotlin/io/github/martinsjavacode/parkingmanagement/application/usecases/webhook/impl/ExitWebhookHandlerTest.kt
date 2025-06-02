package io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetParkingByCoordinatesOrThrowHandler
import io.github.martinsjavacode.parkingmanagement.application.usecases.revenue.UpdateOrInitializeDailyRevenueHandler
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.redis.DistributedLockPort
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

class ExitWebhookHandlerTest : BehaviorSpec({
    val messageSource = mockk<MessageSource>(relaxed = true)
    val traceContext = mockk<TraceContext>(relaxed = true)
    val parkingEventRepository = mockk<ParkingEventRepositoryPort>()
    val getParkingByCoordinatesOrThrowHandler = mockk<GetParkingByCoordinatesOrThrowHandler>()
    val updateDailyRevenueHandler = mockk<UpdateOrInitializeDailyRevenueHandler>()
    val distributedLock = mockk<DistributedLockPort>()

    val exitWebhookHandler =
        ExitWebhookHandler(
            messageSource = messageSource,
            traceContext = traceContext,
            parkingEventRepository = parkingEventRepository,
            getParkingByCoordinatesOrThrowHandler = getParkingByCoordinatesOrThrowHandler,
            updateDailyRevenueHandler = updateDailyRevenueHandler,
            distributedLock = distributedLock,
        )

    beforeTest {
        // Default mocks for all tests
        coEvery { traceContext.traceId() } returns "test-trace-id"
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
        coEvery { distributedLock.checkAndMarkIdempotency(any(), any(), any(), any()) } returns true
        coEvery { distributedLock.acquireLock(any(), any(), any(), any()) } returns true
        coEvery { distributedLock.releaseLock(any(), any(), any()) } returns true
        coEvery { distributedLock.releaseIdempotencyKey(any(), any()) } returns true
    }

    afterTest {
        clearAllMocks()
    }

    given("An exit webhook event") {
        val exitEvent =
            WebhookEvent(
                id = "test-event-id",
                licensePlate = "ABC1234",
                lat = 10.0,
                lng = 20.0,
                eventType = EventType.EXIT,
                entryTime = null,
                exitTime = LocalDateTime.now(),
            )

        `when`("No parked event is found for the license plate") {
            coEvery { parkingEventRepository.findAllByLicensePlate(any()) } returns emptyFlow()

            then("It should throw NoParkedEventFoundException") {
                shouldThrow<NoParkedEventFoundException> {
                    exitWebhookHandler.handle(exitEvent)
                }
            }
        }

        `when`("A parked event is found for the license plate") {
            val parkedEvent =
                ParkingEvent(
                    id = 1L,
                    licensePlate = "ABC1234",
                    latitude = 10.0,
                    longitude = 20.0,
                    eventType = EventType.PARKED,
                    entryTime = LocalDateTime.now().minusHours(2),
                    priceMultiplier = 1.5,
                )

            val mockParking =
                mockk<Parking> {
                    every { basePrice } returns BigDecimal("10.00")
                    every { durationLimitMinutes } returns 120
                }

            val mockRevenue = mockk<Revenue>()

            coEvery { parkingEventRepository.findAllByLicensePlate(any()) } returns flowOf(parkedEvent)
            coEvery { getParkingByCoordinatesOrThrowHandler.handle(any(), any()) } returns mockParking
            coEvery { parkingEventRepository.save(any()) } returns mockk()
            coEvery { updateDailyRevenueHandler.handle(any(), any(), any(), any()) } returns mockRevenue

            then("It should process the exit event successfully") {
                exitWebhookHandler.handle(exitEvent)

                coVerify {
                    parkingEventRepository.save(
                        match<ParkingEvent> {
                            it.eventType == EventType.EXIT &&
                                it.licensePlate == "ABC1234" &&
                                it.exitTime != null
                        },
                    )
                }

                coVerify {
                    updateDailyRevenueHandler.handle(
                        eq(EventType.EXIT),
                        eq(10.0),
                        eq(20.0),
                        any(),
                    )
                }
            }
        }
    }
})
