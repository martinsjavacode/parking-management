package io.github.martinsjavacode.parkingmanagement.application.service

import io.github.martinsjavacode.parkingmanagement.domain.gateway.client.ExternalParkingApiPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingRepositoryPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class ParkingService(
    private val externalParkingApiPort: ExternalParkingApiPort,
    private val parkingRepositoryPort: ParkingRepositoryPort,
) {

    // Dispatcher otimizado para operações de IO com limite de paralelismo
    @OptIn(ExperimentalCoroutinesApi::class)
    private val ioDispatcher = Dispatchers.IO.limitedParallelism(10)

    suspend fun fetchAndSaveParking() {
        withContext(ioDispatcher) {
            val response = externalParkingApiPort.fetchGarageConfig()

            response.collect { parking ->
                parkingRepositoryPort.upsert(parking)
            }
        }
    }
}
