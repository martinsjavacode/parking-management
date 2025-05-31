package io.github.martinsjavacode.parkingmanagement.unit.adapters.outbound.client

import io.github.martinsjavacode.parkingmanagement.adapters.outbound.client.ParkingClientAdapter
import io.github.martinsjavacode.parkingmanagement.adapters.outbound.client.response.ParkingAndSpotsResponse
import io.github.martinsjavacode.parkingmanagement.adapters.outbound.client.response.ParkingDataResponse
import io.github.martinsjavacode.parkingmanagement.adapters.outbound.client.response.ParkingSpotDataResponse
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal

class ParkingClientAdapterTest : DescribeSpec({

    // Mock WebClient and related components
    val webClientBuilder = mockk<WebClient.Builder>()
    val webClient = mockk<WebClient>()
    val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
    val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
    val responseSpec = mockk<WebClient.ResponseSpec>()

    // Create the adapter with a mocked WebClient
    val apiBaseUrl = "http://example.com/api"
    val adapter = ParkingClientAdapter(apiBaseUrl)

    // Use reflection to set the mocked WebClient
    val webClientField = ParkingClientAdapter::class.java.getDeclaredField("webClient")
    webClientField.isAccessible = true
    webClientField.set(adapter, webClient)

    beforeTest {
        // Mock the WebClient chain
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("/garage") } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
    }

    describe("ParkingClientAdapter") {

        context("fetchGarageConfig") {
            it("should fetch and convert garage configuration") {
                // Given
                val parkingDataResponse =
                    ParkingDataResponse(
                        sector = "A",
                        basePrice = BigDecimal("10.00"),
                        maxCapacity = 100,
                        openHour = "08:00",
                        closeHour = "20:00",
                        durationLimitMinutes = 60,
                    )

                val parkingSpotDataResponse =
                    ParkingSpotDataResponse(
                        id = 1L,
                        sector = "A",
                        lat = 45.0,
                        lng = 90.0,
                    )

                val response =
                    ParkingAndSpotsResponse(
                        parking = listOf(parkingDataResponse),
                        parkingSpots = listOf(parkingSpotDataResponse),
                    )

                // Mock the Mono response
                val mono = mockk<Mono<ParkingAndSpotsResponse>>()
                every { responseSpec.bodyToMono(ParkingAndSpotsResponse::class.java) } returns mono

                // Mock the awaitSingle extension function
                mockkStatic("kotlinx.coroutines.reactor.MonoKt")
                coEvery { mono.awaitSingle() } returns response

                // When
                val result = adapter.fetchGarageConfig()
                val resultList = result.toList()

                // Then
                resultList.size shouldBe 1
                resultList[0].sector shouldBe "A"
                resultList[0].basePrice shouldBe BigDecimal("10.00")
                resultList[0].maxCapacity shouldBe 100
                resultList[0].durationLimitMinutes shouldBe 60
            }

            it("should handle empty response") {
                // Given
                val response =
                    ParkingAndSpotsResponse(
                        parking = emptyList(),
                        parkingSpots = emptyList(),
                    )

                // Mock the Mono response
                val mono = mockk<Mono<ParkingAndSpotsResponse>>()
                every { responseSpec.bodyToMono(ParkingAndSpotsResponse::class.java) } returns mono

                // Mock the awaitSingle extension function
                mockkStatic("kotlinx.coroutines.reactor.MonoKt")
                coEvery { mono.awaitSingle() } returns response

                // When
                val result = adapter.fetchGarageConfig()
                val resultList = result.toList()

                // Then
                resultList.size shouldBe 0
            }

            it("should filter spots by sector") {
                // Given
                val parkingDataResponseA =
                    ParkingDataResponse(
                        sector = "A",
                        basePrice = BigDecimal("10.00"),
                        maxCapacity = 100,
                        openHour = "08:00",
                        closeHour = "20:00",
                        durationLimitMinutes = 60,
                    )

                val parkingDataResponseB =
                    ParkingDataResponse(
                        sector = "B",
                        basePrice = BigDecimal("15.00"),
                        maxCapacity = 50,
                        openHour = "09:00",
                        closeHour = "21:00",
                        durationLimitMinutes = 120,
                    )

                val parkingSpotDataResponseA1 =
                    ParkingSpotDataResponse(
                        id = 1L,
                        sector = "A",
                        lat = 45.0,
                        lng = 90.0,
                    )

                val parkingSpotDataResponseA2 =
                    ParkingSpotDataResponse(
                        id = 2L,
                        sector = "A",
                        lat = 46.0,
                        lng = 91.0,
                    )

                val parkingSpotDataResponseB =
                    ParkingSpotDataResponse(
                        id = 3L,
                        sector = "B",
                        lat = 47.0,
                        lng = 92.0,
                    )

                val response =
                    ParkingAndSpotsResponse(
                        parking = listOf(parkingDataResponseA, parkingDataResponseB),
                        parkingSpots =
                            listOf(
                                parkingSpotDataResponseA1,
                                parkingSpotDataResponseA2,
                                parkingSpotDataResponseB,
                            ),
                    )

                // Mock the Mono response
                val mono = mockk<Mono<ParkingAndSpotsResponse>>()
                every { responseSpec.bodyToMono(ParkingAndSpotsResponse::class.java) } returns mono

                // Mock the awaitSingle extension function
                mockkStatic("kotlinx.coroutines.reactor.MonoKt")
                coEvery { mono.awaitSingle() } returns response

                // When
                val result = adapter.fetchGarageConfig()
                val resultList = result.toList()

                // Then
                resultList.size shouldBe 2

                // Check first parking (sector A)
                resultList[0].sector shouldBe "A"
                resultList[0].basePrice shouldBe BigDecimal("10.00")

                // Check second parking (sector B)
                resultList[1].sector shouldBe "B"
                resultList[1].basePrice shouldBe BigDecimal("15.00")
            }
        }
    }
})
