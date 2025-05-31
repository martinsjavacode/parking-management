package io.github.martinsjavacode.parkingmanagement.adapters.outbound.client

import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toDomain
import io.github.martinsjavacode.parkingmanagement.adapters.outbound.client.response.ParkingAndSpotsResponse
import io.github.martinsjavacode.parkingmanagement.domain.gateway.client.ExternalParkingApiPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.Parking
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingSpot
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ParkingClientAdapter(
    @Value("\${client.garage.url}")
    private val apiBaseUrl: String,
) : ExternalParkingApiPort {
    private val logger = loggerFor<ParkingClientAdapter>()
    private val webClient = WebClient.create(apiBaseUrl)

    override suspend fun fetchGarageConfig(): Flow<Parking> {
        val response =
            webClient.get()
                .uri("/garage")
                .retrieve()
                .bodyToMono(ParkingAndSpotsResponse::class.java)
                .awaitSingle()

        val parkingSpots =
            response.parkingSpots
                .asFlow()
                .map { spot ->
                    ParkingSpot(
                        sector = spot.sector,
                        latitude = spot.lat,
                        longitude = spot.lng,
                    )
                }

        return response.parking
            .asFlow()
            .map { parking ->
                val spots = parkingSpots.filter { parkingSpot -> parkingSpot.sector == parking.sector }
                parking.toDomain(spots)
            }
    }

    private fun fetchGarageConfigFallback(e: Exception): Flow<Parking> {
        logger.warn("Fallback method called due to exception: ${e.message}")
        return emptyList<Parking>().asFlow()
    }
}
