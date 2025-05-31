package io.github.martinsjavacode.parkingmanagement.service.plate.impl

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.service.plate.GetPlateStatusHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class GetPlateStatusHandlerImpl(
    private val parkingEventRepository: ParkingEventRepositoryPort
) : GetPlateStatusHandler {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    override suspend fun handle(licensePlate: String) {
        val parkingEvents = withContext(dispatcherIO) {
            parkingEventRepository.findAllByLicensePlate(licensePlate)
        }.firstOrNull { it.eventType != EventType.EXIT }

    }
}
