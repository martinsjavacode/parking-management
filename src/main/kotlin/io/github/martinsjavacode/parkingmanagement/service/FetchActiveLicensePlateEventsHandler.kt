package io.github.martinsjavacode.parkingmanagement.service

import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class FetchActiveLicensePlateEventsHandler(
    private val parkingEventRepository: ParkingEventRepositoryPort,
) {
    private val logger = loggerFor<FetchActiveLicensePlateEventsHandler>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    suspend fun handle(
        licensePlate: String,
        latitude: Double?,
        longitude: Double?,
    ) = withContext(dispatcherIO) {
        logger.debug("Checking active parking events for licensePlate={}", licensePlate)
        parkingEventRepository.findActiveParkingEventByLicensePlate(
            licensePlate = licensePlate,
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0,
        )
    }
}
