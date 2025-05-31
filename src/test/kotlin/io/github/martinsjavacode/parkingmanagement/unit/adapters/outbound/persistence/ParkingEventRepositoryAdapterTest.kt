package io.github.martinsjavacode.parkingmanagement.unit.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence.ParkingEventRepositoryAdapter
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.LicensePlateNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingEventNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingEventSaveFailedException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEventEntity
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingEventRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.LocalDateTime

class ParkingEventRepositoryAdapterTest : DescribeSpec({

    val parkingEventRepository = mockk<ParkingEventRepository>()
    val messageSource = mockk<MessageSource>()
    val traceContext = mockk<TraceContext>()

    val adapter =
        ParkingEventRepositoryAdapter(
            messageSource = messageSource,
            traceContext = traceContext,
            parkingEventRepository = parkingEventRepository,
        )

    beforeTest {
        clearAllMocks()

        // Common mocks
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
        every { traceContext.traceId() } returns "test-trace-id"
    }

    describe("ParkingEventRepositoryAdapter") {

        context("save") {
            it("should save parking event successfully") {
                // Given
                val parkingEvent =
                    ParkingEvent(
                        id = null,
                        licensePlate = "ABC1234",
                        latitude = 45.0,
                        longitude = 90.0,
                        entryTime = LocalDateTime.now(),
                        eventType = EventType.ENTRY,
                    )

                val savedParkingEventEntity =
                    ParkingEventEntity(
                        id = 1L,
                        licensePlate = "ABC1234",
                        latitude = 45.0,
                        longitude = 90.0,
                        entryTime = parkingEvent.entryTime,
                        eventType = EventType.ENTRY,
                        priceMultiplier = 1.0,
                        amountPaid = BigDecimal.ZERO,
                    )

                coEvery { parkingEventRepository.save(any()) } returns savedParkingEventEntity

                // When
                adapter.save(parkingEvent)

                // Then
                coVerify(exactly = 1) { parkingEventRepository.save(any()) }
            }

            it("should throw ParkingEventSaveFailedException when saving fails") {
                // Given
                val parkingEvent =
                    ParkingEvent(
                        id = null,
                        licensePlate = "ABC1234",
                        latitude = 45.0,
                        longitude = 90.0,
                        entryTime = LocalDateTime.now(),
                        eventType = EventType.ENTRY,
                    )

                coEvery { parkingEventRepository.save(any()) } throws RuntimeException("Database error")

                // When/Then
                shouldThrow<ParkingEventSaveFailedException> {
                    adapter.save(parkingEvent)
                }

                coVerify(exactly = 1) { parkingEventRepository.save(any()) }
            }
        }

        context("findAllByLicensePlate") {
            it("should return all parking events for a license plate") {
                // Given
                val licensePlate = "ABC1234"
                val parkingEventEntity1 =
                    ParkingEventEntity(
                        id = 1L,
                        licensePlate = licensePlate,
                        latitude = 45.0,
                        longitude = 90.0,
                        entryTime = LocalDateTime.now().minusHours(2),
                        eventType = EventType.ENTRY,
                        priceMultiplier = 1.0,
                        amountPaid = BigDecimal.ZERO,
                    )

                val parkingEventEntity2 =
                    ParkingEventEntity(
                        id = 2L,
                        licensePlate = licensePlate,
                        latitude = 45.0,
                        longitude = 90.0,
                        entryTime = LocalDateTime.now().minusHours(1),
                        eventType = EventType.PARKED,
                        priceMultiplier = 1.1,
                        amountPaid = BigDecimal.ZERO,
                    )

                coEvery {
                    parkingEventRepository.findByLicensePlate(licensePlate)
                } returns flowOf(parkingEventEntity1, parkingEventEntity2)

                // When
                val result = adapter.findAllByLicensePlate(licensePlate)
                val resultList = result.toList()

                // Then
                resultList.size shouldBe 2
                resultList[0].id shouldBe 1L
                resultList[0].licensePlate shouldBe licensePlate
                resultList[0].eventType shouldBe EventType.ENTRY
                resultList[1].id shouldBe 2L
                resultList[1].licensePlate shouldBe licensePlate
                resultList[1].eventType shouldBe EventType.PARKED

                coVerify(exactly = 1) { parkingEventRepository.findByLicensePlate(licensePlate) }
            }

            it("should throw LicensePlateNotFoundException when no events found") {
                // Given
                val licensePlate = "ABC1234"
                coEvery { parkingEventRepository.findByLicensePlate(licensePlate) } returns null

                // When/Then
                shouldThrow<LicensePlateNotFoundException> {
                    adapter.findAllByLicensePlate(licensePlate)
                }

                coVerify(exactly = 1) { parkingEventRepository.findByLicensePlate(licensePlate) }
            }
        }

        context("findLastParkingEventByLicenseAndEventType") {
            it("should return the last parking event by license plate and event type") {
                // Given
                val licensePlate = "ABC1234"
                val eventType = EventType.PARKED
                val parkingEventEntity =
                    ParkingEventEntity(
                        id = 1L,
                        licensePlate = licensePlate,
                        latitude = 45.0,
                        longitude = 90.0,
                        entryTime = LocalDateTime.now().minusHours(1),
                        eventType = eventType,
                        priceMultiplier = 1.1,
                        amountPaid = BigDecimal.ZERO,
                    )

                coEvery {
                    parkingEventRepository.findByLicensePlateAndEventType(licensePlate, eventType)
                } returns parkingEventEntity

                // When
                val result = adapter.findLastParkingEventByLicenseAndEventType(licensePlate, eventType)

                // Then
                result.id shouldBe 1L
                result.licensePlate shouldBe licensePlate
                result.eventType shouldBe eventType

                coVerify(exactly = 1) {
                    parkingEventRepository.findByLicensePlateAndEventType(licensePlate, eventType)
                }
            }

            it("should throw ParkingEventNotFoundException when no event found") {
                // Given
                val licensePlate = "ABC1234"
                val eventType = EventType.PARKED
                coEvery {
                    parkingEventRepository.findByLicensePlateAndEventType(licensePlate, eventType)
                } throws RuntimeException("Not found")

                // When/Then
                shouldThrow<ParkingEventNotFoundException> {
                    adapter.findLastParkingEventByLicenseAndEventType(licensePlate, eventType)
                }

                coVerify(exactly = 1) {
                    parkingEventRepository.findByLicensePlateAndEventType(licensePlate, eventType)
                }
            }
        }

        context("findMostRecentByCoordinates") {
            it("should return the most recent parking event by coordinates") {
                // Given
                val latitude = 45.0
                val longitude = 90.0
                val parkingEventEntity =
                    ParkingEventEntity(
                        id = 1L,
                        licensePlate = "ABC1234",
                        latitude = latitude,
                        longitude = longitude,
                        entryTime = LocalDateTime.now().minusHours(1),
                        eventType = EventType.PARKED,
                        priceMultiplier = 1.1,
                        amountPaid = BigDecimal.ZERO,
                    )

                coEvery {
                    parkingEventRepository.findLastByLatitudeAndLongitude(latitude, longitude)
                } returns parkingEventEntity

                // When
                val result = adapter.findMostRecentByCoordinates(latitude, longitude)

                // Then
                result.id shouldBe 1L
                result.latitude shouldBe latitude
                result.longitude shouldBe longitude

                coVerify(exactly = 1) {
                    parkingEventRepository.findLastByLatitudeAndLongitude(latitude, longitude)
                }
            }

            it("should throw ParkingEventNotFoundException when no event found") {
                // Given
                val latitude = 45.0
                val longitude = 90.0
                coEvery {
                    parkingEventRepository.findLastByLatitudeAndLongitude(latitude, longitude)
                } throws RuntimeException("Not found")

                // When/Then
                shouldThrow<ParkingEventNotFoundException> {
                    adapter.findMostRecentByCoordinates(latitude, longitude)
                }

                coVerify(exactly = 1) {
                    parkingEventRepository.findLastByLatitudeAndLongitude(latitude, longitude)
                }
            }
        }
    }
})
