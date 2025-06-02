package io.github.martinsjavacode.parkingmanagement.domain.rules

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime

/**
 * Object containing the operational rules for the parking system.
 *
 * This singleton object implements business rules related to dynamic pricing,
 * coordinate validation, and parking fee calculation.
 */
object OperationalRules {
    /**
     * Determines the price multiplier based on the parking lot occupancy rate.
     *
     * Implements the dynamic pricing rule:
     * - Occupancy < 25%: 10% discount (multiplier 0.9)
     * - Occupancy between 25% and 50%: base price (multiplier 1.0)
     * - Occupancy between 50% and 75%: 10% surcharge (multiplier 1.1)
     * - Occupancy > 75%: 25% surcharge (multiplier 1.25)
     *
     * @param occupancyRate Parking lot occupancy rate as a percentage (0-100)
     * @return Price multiplier to be applied
     */
    fun priceMultiplierForOccupancyRate(occupancyRate: Int): Double =
        when {
            occupancyRate < 25 -> 0.9
            occupancyRate < 50 -> 1.0
            occupancyRate < 75 -> 1.1
            else -> 1.25
        }

    /**
     * Validates if the provided geographic coordinates are valid.
     *
     * Ensures that coordinates are not null and within valid bounds:
     * - Latitude: between -90.0 and 90.0
     * - Longitude: between -180.0 and 180.0
     *
     * @param latitude Latitude coordinate to validate
     * @param longitude Longitude coordinate to validate
     * @throws IllegalStateException If the coordinates are invalid
     */
    fun assertValidCoordinates(
        latitude: Double?,
        longitude: Double?,
    ) {
        check(latitude != null && longitude != null) { "Latitude and longitude cannot be null" }
        check(latitude in -90.0..90.0 && longitude in -180.0..180.0) {
            "Invalid latitude or longitude"
        }
    }

    /**
     * Calculates the parking fee.
     *
     * The calculation is based on the parking duration, base price, duration limit,
     * and dynamic price multiplier.
     *
     * @param entryTime Vehicle entry time
     * @param exitTime Vehicle exit time
     * @param basePrice Base price for parking
     * @param durationLimitMinutes Time limit in minutes for the base charge period
     * @param priceMultiplier Price multiplier based on occupancy
     * @return Parking fee to be charged
     */
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
