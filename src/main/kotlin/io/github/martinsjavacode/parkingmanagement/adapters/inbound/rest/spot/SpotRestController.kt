package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.spot

import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toResponse
import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.spot.request.SpotStatusRequest
import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.spot.response.SpotStatusResponse
import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetParkingSpotStatusHandler
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SpotRestController(
    private val getParkingSpotStatusHandler: GetParkingSpotStatusHandler
) {

    @PostMapping("/spot-status")
    suspend fun changeSpotStatus(@RequestBody request: SpotStatusRequest): ResponseEntity<SpotStatusResponse> {
        val spotStatus = getParkingSpotStatusHandler.handle(request.lat, request.lng)
        return ResponseEntity.ok(spotStatus.toResponse())
    }
}
