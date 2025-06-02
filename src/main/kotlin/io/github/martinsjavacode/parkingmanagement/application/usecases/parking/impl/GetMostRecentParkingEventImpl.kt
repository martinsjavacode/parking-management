package io.github.martinsjavacode.parkingmanagement.application.usecases.parking.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.GetMostRecentParkingEvent
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class GetMostRecentParkingEventImpl(
    private val parkingEventRepository: ParkingEventRepositoryPort,
) : GetMostRecentParkingEvent {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    override suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): ParkingEvent =
        withContext(dispatcherIO) {
            parkingEventRepository.findMostRecentByCoordinates(latitude, longitude)
        }
}
