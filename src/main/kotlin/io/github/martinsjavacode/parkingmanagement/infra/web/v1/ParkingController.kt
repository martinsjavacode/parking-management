package io.github.martinsjavacode.parkingmanagement.infra.web.v1

import io.github.martinsjavacode.parkingmanagement.application.service.ParkingService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController("parking")
@RequestMapping("/v1/parking")
class ParkingController(
    private val parkingService: ParkingService
) {

    @PostMapping
    suspend fun fetchAndSave() {
        parkingService.fetchAndSaveParking()
    }

}
