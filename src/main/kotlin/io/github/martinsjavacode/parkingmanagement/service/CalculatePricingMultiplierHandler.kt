package io.github.martinsjavacode.parkingmanagement.service

import io.github.martinsjavacode.parkingmanagement.domain.extension.percentOf
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.springframework.stereotype.Service

@Service
class CalculatePricingMultiplierHandler(
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort,
) {
    suspend fun execute(
        latitude: Double,
        longitude: Double,
    ): Double {
        OperationalRules.checkCoordinates(latitude, longitude)

        val occupancyRate = calculateOccupancyRate(latitude, longitude)
        return OperationalRules.priceMultiplier(occupancyRate)
    }

    private suspend fun calculateOccupancyRate(
        latitude: Double,
        longitude: Double,
    ): Int {
        val (maxCapacity, spotOccupancy) =
            parkingCustomQueryRepository.findParkingCapacityAndOccupancy(
                latitude,
                longitude,
            )

        return spotOccupancy.percentOf(maxCapacity)
    }
}
