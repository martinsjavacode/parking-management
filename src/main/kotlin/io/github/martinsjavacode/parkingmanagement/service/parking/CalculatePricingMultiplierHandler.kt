package io.github.martinsjavacode.parkingmanagement.service.parking

interface CalculatePricingMultiplierHandler {
    suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): Double
}
