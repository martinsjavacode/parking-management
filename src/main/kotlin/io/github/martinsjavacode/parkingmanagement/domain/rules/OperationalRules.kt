package io.github.martinsjavacode.parkingmanagement.domain.rules

object OperationalRules {
    fun calculateDynamicPrice(
        basePrice: Double,
        priceMultiplier: Double,
    ): Double = basePrice * priceMultiplier

    fun isSectorOpen(
        maxCapacity: Int,
        currentOccupancy: Int,
    ): Boolean = currentOccupancy < maxCapacity

    fun priceMultiplier(occupancyRate: Int): Double =
        when {
            occupancyRate < 25 -> 0.9
            occupancyRate < 50 -> 1.0
            occupancyRate < 75 -> 1.1
            else -> 1.25
        }

    fun checkCoordinates(latitude: Double?, longitude: Double?) {
        val notNull = latitude != null && longitude != null
        check(notNull && (latitude in -90.0..90.0) && (longitude in -180.0..180.0)) {
            "Invalid latitude or longitude"
        }
    }
}
