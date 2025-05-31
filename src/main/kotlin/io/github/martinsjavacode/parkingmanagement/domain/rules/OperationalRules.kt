package io.github.martinsjavacode.parkingmanagement.domain.rules

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime

object OperationalRules {
    fun priceMultiplierForOccupancyRate(occupancyRate: Int): Double =
        when {
            occupancyRate < 25 -> 0.9
            occupancyRate < 50 -> 1.0
            occupancyRate < 75 -> 1.1
            else -> 1.25
        }

    fun assertValidCoordinates(
        latitude: Double?,
        longitude: Double?,
    ) {
        val notNull = latitude != null && longitude != null
        check(notNull && (latitude in -90.0..90.0) && (longitude in -180.0..180.0)) {
            "Invalid latitude or longitude"
        }
    }

    fun calculateParkingFee(
        entryTime: LocalDateTime,
        exitTime: LocalDateTime,
        basePrice: BigDecimal,
        durationLimitMinutes: Int,
        priceMultiplier: Double,
    ): BigDecimal {
        // Calculate the duration in minutes
        val durationMinutes = Duration.between(entryTime, exitTime).toMinutes()

        // Convert the duration into proportional "periods" based on the duration limit
        val period =
            BigDecimal(durationMinutes).divide(
                // Convert duration limit to BigDecimal
                BigDecimal(durationLimitMinutes),
                // Division precision
                10,
                RoundingMode.HALF_UP,
            )

        // Calculate the base amount proportional to the period
        val amountBase = period.multiply(basePrice)

        // Multiply by the price multiplier and return the final amount with 2 decimal places
        return amountBase.multiply(BigDecimal.valueOf(priceMultiplier))
            .setScale(2, RoundingMode.HALF_UP)
    }
}
