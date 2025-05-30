package io.github.martinsjavacode.parkingmanagement.infra.rest.v1

import io.github.martinsjavacode.parkingmanagement.service.parking.ParkingSyncHandler
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
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    suspend fun fetchAndSave() {
        parkingSyncHandler.handle()
    }
}
