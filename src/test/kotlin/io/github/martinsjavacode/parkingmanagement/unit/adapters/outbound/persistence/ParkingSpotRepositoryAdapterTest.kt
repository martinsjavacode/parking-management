package io.github.martinsjavacode.parkingmanagement.unit.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence.ParkingSpotRepositoryAdapter
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.ParkingSpotNotFoundException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingSpotEntity
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingSpotRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

class ParkingSpotRepositoryAdapterTest : DescribeSpec({

    val parkingSpotRepository = mockk<ParkingSpotRepository>()
    val messageSource = mockk<MessageSource>()
    val traceContext = mockk<TraceContext>()

    val adapter =
        ParkingSpotRepositoryAdapter(
            messageSource = messageSource,
            traceContext = traceContext,
            parkingSpotRepository = parkingSpotRepository,
        )

    beforeTest {
        clearAllMocks()

        // Common mocks
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
        every { traceContext.traceId() } returns "test-trace-id"
    }

    describe("ParkingSpotRepositoryAdapter") {

        context("findByCoordinates") {
            it("should return parking spot by coordinates") {
                // Given
                val latitude = 45.0
                val longitude = 90.0
                val parkingSpotEntity =
                    ParkingSpotEntity(
                        id = 1L,
                        parkingId = 1L,
                        latitude = latitude,
                        longitude = longitude,
                    )

                coEvery {
                    parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
                } returns parkingSpotEntity

                // When
                val result = adapter.findByCoordinates(latitude, longitude)

                // Then
                result.id shouldBe 1L
                result.parkingId shouldBe 1L
                result.latitude shouldBe latitude
                result.longitude shouldBe longitude

                coVerify(exactly = 1) {
                    parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
                }
            }

            it("should throw ParkingSpotNotFoundException when spot not found") {
                // Given
                val latitude = 45.0
                val longitude = 90.0

                coEvery {
                    parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
                } throws RuntimeException("Not found")

                mockkStatic(LocaleContextHolder::class)
                every { LocaleContextHolder.getLocale() } returns java.util.Locale.ENGLISH

                // When/Then
                shouldThrow<ParkingSpotNotFoundException> {
                    adapter.findByCoordinates(latitude, longitude)
                }

                coVerify(exactly = 1) {
                    parkingSpotRepository.findByLatitudeAndLongitude(latitude, longitude)
                }
            }
        }
    }
})
