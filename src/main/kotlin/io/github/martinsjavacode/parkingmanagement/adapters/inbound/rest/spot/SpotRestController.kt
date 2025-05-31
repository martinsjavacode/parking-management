package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.spot

import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toResponse
import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.spot.request.SpotStatusRequest
import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.spot.response.SpotStatusResponse
import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetParkingSpotStatusHandler
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SpotRestController(
    private val getParkingSpotStatusHandler: GetParkingSpotStatusHandler,
) {
    @Operation(
        summary = "Check the status of a parking spot",
        tags = ["Parking Spot"],
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Coordinates of the parking spot to be queried",
                required = true,
                content = [
                    io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        schema = io.swagger.v3.oas.annotations.media.Schema(implementation = SpotStatusRequest::class),
                    ),
                ],
            ),
        responses = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Parking spot status",
                content = [
                    io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        schema = io.swagger.v3.oas.annotations.media.Schema(implementation = SpotStatusResponse::class),
                    ),
                ],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid event data",
                content = [
                    io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        schema = io.swagger.v3.oas.annotations.media.Schema(),
                    ),
                ],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [
                    io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        schema = io.swagger.v3.oas.annotations.media.Schema(),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/spot-status")
    suspend fun changeSpotStatus(
        @RequestBody request: SpotStatusRequest,
    ): ResponseEntity<SpotStatusResponse> {
        val spotStatus = getParkingSpotStatusHandler.handle(request.lat, request.lng)
        return ResponseEntity.ok(spotStatus.toResponse())
    }
}
