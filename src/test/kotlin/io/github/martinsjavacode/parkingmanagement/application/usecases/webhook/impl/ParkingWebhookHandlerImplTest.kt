package io.github.martinsjavacode.parkingmanagement.application.usecases.webhook.impl

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.webhook.WebhookEvent
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import java.time.LocalDateTime

class ParkingWebhookHandlerImplTest : BehaviorSpec({
    val entryWebhookHandler = mockk<EntryWebhookHandler>(relaxed = true)
    val parkedWebhookHandler = mockk<ParkedWebhookHandler>(relaxed = true)
    val exitWebhookHandler = mockk<ExitWebhookHandler>(relaxed = true)
    
    val parkingWebhookHandler = ParkingWebhookHandlerImpl(
        entryWebhookHandler = entryWebhookHandler,
        parkedWebhookHandler = parkedWebhookHandler,
        exitWebhookHandler = exitWebhookHandler
    )
    
    beforeTest {
        clearAllMocks()
    }
    
    given("A webhook event") {
        val now = LocalDateTime.now()
        val licensePlate = "ABC1234"
        val latitude = -23.561684
        val longitude = -46.655981
        
        `when`("The event is an ENTRY event") {
            val entryEvent = WebhookEvent(
                licensePlate = licensePlate,
                lat = null,
                lng = null,
                entryTime = now,
                exitTime = null,
                eventType = EventType.ENTRY
            )
            
            then("Should delegate to the EntryWebhookHandler") {
                parkingWebhookHandler.execute(entryEvent)
                
                coVerify(exactly = 1) { entryWebhookHandler.handle(entryEvent) }
                coVerify(exactly = 0) { parkedWebhookHandler.handle(any()) }
                coVerify(exactly = 0) { exitWebhookHandler.handle(any()) }
            }
        }
        
        `when`("The event is a PARKED event") {
            val parkedEvent = WebhookEvent(
                licensePlate = licensePlate,
                lat = latitude,
                lng = longitude,
                entryTime = null,
                exitTime = null,
                eventType = EventType.PARKED
            )
            
            then("Should delegate to the ParkedWebhookHandler") {
                parkingWebhookHandler.execute(parkedEvent)
                
                coVerify(exactly = 0) { entryWebhookHandler.handle(any()) }
                coVerify(exactly = 1) { parkedWebhookHandler.handle(parkedEvent) }
                coVerify(exactly = 0) { exitWebhookHandler.handle(any()) }
            }
        }
        
        `when`("The event is an EXIT event") {
            val exitEvent = WebhookEvent(
                licensePlate = licensePlate,
                lat = null,
                lng = null,
                entryTime = null,
                exitTime = now,
                eventType = EventType.EXIT
            )
            
            then("Should delegate to the ExitWebhookHandler") {
                parkingWebhookHandler.execute(exitEvent)
                
                coVerify(exactly = 0) { entryWebhookHandler.handle(any()) }
                coVerify(exactly = 0) { parkedWebhookHandler.handle(any()) }
                coVerify(exactly = 1) { exitWebhookHandler.handle(exitEvent) }
            }
        }
    }
})
