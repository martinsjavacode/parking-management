package io.github.martinsjavacode.parkingmanagement.domain.model.parking

/**
 * Represents the capacity and current occupancy of a parking lot.
 *
 * This data class is used to track the maximum capacity of a parking lot
 * and its current occupancy, which is essential for calculating dynamic pricing.
 *
 * @property maxCapacity The maximum number of vehicles the parking lot can accommodate
 * @property spotOccupancy The current number of occupied spots in the parking lot
 */
data class ParkingCapacityAndOccupancy(
    val maxCapacity: Int,
    val spotOccupancy: Int,
)
