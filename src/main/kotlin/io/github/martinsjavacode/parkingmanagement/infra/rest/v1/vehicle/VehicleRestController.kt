package io.github.martinsjavacode.parkingmanagement.infra.rest.v1.vehicle

import io.github.martinsjavacode.parkingmanagement.domain.extension.vehicle.toResponse
import io.github.martinsjavacode.parkingmanagement.infra.rest.v1.vehicle.request.LicensePlateRequest
import io.github.martinsjavacode.parkingmanagement.infra.rest.v1.vehicle.response.PlateStatusResponse
import io.github.martinsjavacode.parkingmanagement.service.plate.GetPlateStatusHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class VehicleRestController(
    private val getPlateStatusHandler: GetPlateStatusHandler,
) {
    @PostMapping("/plate-status")
    suspend fun retrievePlateStatus(
        @RequestBody request: LicensePlateRequest,
    ): ResponseEntity<PlateStatusResponse> {
        val plateStatus = getPlateStatusHandler.handle(request.licensePlate)
        return ResponseEntity.ok(plateStatus.toResponse())
    }
}
