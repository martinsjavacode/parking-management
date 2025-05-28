package io.github.martinsjavacode.parkingmanagement.domain.model

data class ParkingSpot(
    val id: Long,
    val parkingId: Long?,
    val sector: String,
    val latitude: Double,
    val longitude: Double
)
