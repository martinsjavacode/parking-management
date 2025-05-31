package io.github.martinsjavacode.parkingmanagement.service.plate.impl

import io.github.martinsjavacode.parkingmanagement.service.plate.GetPlateStatusHandler
import org.springframework.stereotype.Service

@Service
class GetPlateStatusHandlerImpl : GetPlateStatusHandler {
    override suspend fun handle(licensePlate: String) {
    }
}
