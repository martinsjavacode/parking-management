package io.github.martinsjavacode.parkingmanagement.service.parking.impl

import io.github.martinsjavacode.parkingmanagement.domain.extension.percentOf
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.rules.OperationalRules
import io.github.martinsjavacode.parkingmanagement.service.parking.CalculatePricingMultiplierHandler
import org.springframework.stereotype.Service

@Service
class CalculatePricingMultiplierHandlerImpl(
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort,
) : CalculatePricingMultiplierHandler {
    override suspend fun handle(
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
