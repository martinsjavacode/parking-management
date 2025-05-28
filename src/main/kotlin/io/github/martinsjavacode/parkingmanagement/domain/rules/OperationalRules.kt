package io.github.martinsjavacode.parkingmanagement.domain.rules

object OperationalRules {
    fun calculateDynamicPrice(basePrice: Double, occupancyRate: Double): Double =
        when {
            occupancyRate < .25 -> basePrice * 0.9
            occupancyRate < .5 -> basePrice
            occupancyRate < .75 -> basePrice * 1.1
            else -> basePrice * 1.25
        }

    fun isSectorOpen(maxCapacity: Int, currentOccupancy: Int): Boolean =
        currentOccupancy < maxCapacity
}
