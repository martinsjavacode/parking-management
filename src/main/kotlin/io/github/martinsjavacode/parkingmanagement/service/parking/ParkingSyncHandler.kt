package io.github.martinsjavacode.parkingmanagement.service.parking

import io.github.martinsjavacode.parkingmanagement.domain.gateway.client.ExternalParkingApiPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingRepositoryPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ParkingSyncHandler(
    private val externalParkingApiPort: ExternalParkingApiPort,
    private val parkingRepositoryPort: ParkingRepositoryPort,
) {
    // Dispatcher optimized for IO operation with parallelism limits
    @OptIn(ExperimentalCoroutinesApi::class)
    private val ioDispatcher = Dispatchers.IO.limitedParallelism(10)

    @Transactional
    suspend fun handle() {
        withContext(ioDispatcher) {
            val response = externalParkingApiPort.fetchGarageConfig()

            response.collect { parking ->
                parkingRepositoryPort.upsert(parking)
            }
        }
    }
}
