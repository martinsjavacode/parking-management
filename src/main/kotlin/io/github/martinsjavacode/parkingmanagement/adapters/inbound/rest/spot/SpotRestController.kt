package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.spot

import io.github.martinsjavacode.parkingmanagement.adapters.extension.parking.toResponse
import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.spot.request.SpotStatusRequest
import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.spot.response.SpotStatusResponse
import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetParkingSpotStatusHandler
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST Controller for parking spot-related operations.
 *
 * Provides endpoints to query information about specific parking spots
 * in the system.
 *
 * @property getParkingSpotStatusHandler Handler to fetch parking spot status
 */
@RestController
class SpotRestController(
    private val getParkingSpotStatusHandler: GetParkingSpotStatusHandler,
) {
    /**
     * Endpoint to query the status of a parking spot by coordinates.
     *
     * @param latitude Latitude of the parking spot
     * @param longitude Longitude of the parking spot
     * @return Response with the parking spot status
     * @throws ParkingSpotNotFoundException If no parking spot is found at the given coordinates
     */
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
    @GetMapping("/spots/status")
    suspend fun changeSpotStatus(
        @RequestParam(value = "lat", required = true) latitude: Double,
        @RequestParam(value = "lng", required = true) longitude: Double,
    ): ResponseEntity<SpotStatusResponse> {
        val spotStatus = getParkingSpotStatusHandler.handle(latitude, longitude)
        return ResponseEntity.ok(spotStatus.toResponse())
    }
}
