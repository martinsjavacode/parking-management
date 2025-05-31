package io.github.martinsjavacode.parkingmanagement.application.usecases.parking.impl

import io.github.martinsjavacode.parkingmanagement.application.usecases.parking.ParkingSyncHandler
import io.github.martinsjavacode.parkingmanagement.domain.gateway.client.ExternalParkingApiPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingSpotRepositoryPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ParkingSyncHandlerImpl(
    private val externalParkingApi: ExternalParkingApiPort,
    private val parkingRepository: ParkingRepositoryPort,
    private val parkingSpotRepository: ParkingSpotRepositoryPort
) : ParkingSyncHandler {
    // Dispatcher optimized for IO operation with parallelism limits
    @OptIn(ExperimentalCoroutinesApi::class)
    private val ioDispatcher = Dispatchers.IO.limitedParallelism(10)

    @Transactional
    override suspend fun handle() {
        withContext(ioDispatcher) {
            externalParkingApi.fetchGarageConfig()
        }.collect { parking ->
            runCatching {
                parkingRepository.findBySectorName(parking.sector)
            }.onSuccess { parkingFound ->
                parking.spots.collect { spot ->
                    runCatching {
                        parkingSpotRepository.findByCoordinates(spot.latitude, spot.longitude)
                    }.onFailure {
                        parkingSpotRepository.save(spot.copy(parkingId = parkingFound.id))
                    }
                }
            }.onFailure {
                parkingRepository.upsert(parking)
            }
        }
    }
}
