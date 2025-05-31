package io.github.martinsjavacode.parkingmanagement.service.parking.impl

import io.github.martinsjavacode.parkingmanagement.domain.gateway.client.ExternalParkingApiPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingRepositoryPort
import io.github.martinsjavacode.parkingmanagement.service.parking.ParkingSyncHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ParkingSyncHandlerImpl(
    private val externalParkingApiPort: ExternalParkingApiPort,
    private val parkingRepositoryPort: ParkingRepositoryPort,
) : ParkingSyncHandler {
    // Dispatcher optimized for IO operation with parallelism limits
    @OptIn(ExperimentalCoroutinesApi::class)
    private val ioDispatcher = Dispatchers.IO.limitedParallelism(10)

    @Transactional
    override suspend fun handle() {
        withContext(ioDispatcher) {
            val response = externalParkingApiPort.fetchGarageConfig()

            response.collect { parking ->
                parkingRepositoryPort.upsert(parking)
            }
        }
    }
}
