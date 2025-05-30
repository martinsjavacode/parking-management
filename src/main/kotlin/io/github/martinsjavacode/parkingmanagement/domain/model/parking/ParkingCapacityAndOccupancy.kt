package io.github.martinsjavacode.parkingmanagement.domain.model.parking

data class ParkingCapacityAndOccupancy(
    val maxCapacity: Int,
    val spotOccupancy: Int,
)
