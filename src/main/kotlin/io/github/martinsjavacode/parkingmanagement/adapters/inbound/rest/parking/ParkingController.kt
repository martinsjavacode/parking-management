package io.github.martinsjavacode.parkingmanagement.adapters.inbound.rest.parking

import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.ParkingSyncHandler
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController("parking")
@RequestMapping("/api/v1/parking")
class ParkingController(
    private val parkingSyncHandler: ParkingSyncHandler,
) {
    @Suppress("ktlint:standard:max-line-length")
    @Operation(
        summary = "Fetch garage data from simulator and save to database",
        tags = ["Parking"],
        description = "This endpoint fetches garage data from the external simulator `/garage` endpoint and saves the information into the `parking` and `parking_spots` tables.",
    )
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    suspend fun fetchAndSave() {
        parkingSyncHandler.handle()
    }
}
