package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.vehicle

import io.github.martinsjavacode.parkingmanagement.adapters.extension.vehicle.toResponse
import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.vehicle.request.LicensePlateRequest
import io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.vehicle.response.PlateStatusResponse
import io.github.martinsjavacode.parkingmanagement.application.usecases.plate.GetPlateStatusHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

/**
 * REST Controller for vehicle-related operations.
 *
 * Provides endpoints to query information about parked vehicles
 * in the system.
 *
 * @property getPlateStatusHandler Handler to fetch vehicle status by license plate
 */
@RestController
class VehicleRestController(
    private val getPlateStatusHandler: GetPlateStatusHandler,
) {
    /**
     * Endpoint to query the status of a vehicle by its license plate.
     *
     * @param request Request containing the vehicle's license plate
     * @return Response with the vehicle status
     * @throws LicensePlateNotFoundException If no vehicle with the provided license plate is found
     */
    @Operation(
        summary = "Get vehicle status by license plate",
        tags = ["Vehicle"],
        responses = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Vehicle status retrieved successfully",
                content = [
                    io.swagger.v3.oas.annotations.media.Content(
                        mediaType = "application/json",
                        schema =
                            io.swagger.v3.oas.annotations.media.Schema(
                                implementation = PlateStatusResponse::class,
                            ),
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
    @PostMapping("/plate-status")
    suspend fun retrievePlateStatus(
        @RequestBody request: LicensePlateRequest,
    ): ResponseEntity<PlateStatusResponse> {
        val plateStatus = getPlateStatusHandler.handle(request.licensePlate)
        return ResponseEntity.ok(plateStatus.toResponse())
    }
}
