package io.github.martinsjavacode.parkingmanagement.application.usecases.parking

interface CalculatePricingMultiplierHandler {
    suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): Double
}
