package io.github.martinsjavacode.parkingmanagement.service.parking.impl

import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingEventRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import io.github.martinsjavacode.parkingmanagement.loggerFor
import io.github.martinsjavacode.parkingmanagement.service.parking.FetchActiveLicensePlateEventsHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class FetchActiveLicensePlateEventsHandlerImpl(
    private val parkingEventRepository: ParkingEventRepositoryPort,
) : FetchActiveLicensePlateEventsHandler {
    private val logger = loggerFor<FetchActiveLicensePlateEventsHandlerImpl>()

    override suspend fun handle(
        licensePlate: String,
        latitude: Double?,
        longitude: Double?,
    ): Flow<ParkingEvent> {
        logger.debug("Checking active parking events for licensePlate={}", licensePlate)
        return parkingEventRepository.findActiveParkingEventByLicensePlate(
            licensePlate = licensePlate,
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0,
        )
    }
}
