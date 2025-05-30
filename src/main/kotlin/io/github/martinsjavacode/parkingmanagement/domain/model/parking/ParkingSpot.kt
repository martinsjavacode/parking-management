package io.github.martinsjavacode.parkingmanagement.domain.model.parking

data class ParkingSpot(
    val id: Long?,
    val parkingId: Long?,
    val sector: String? = null,
    var latitude: Double,
    var longitude: Double,
) {
    constructor(sector: String, latitude: Double, longitude: Double) : this(
        id = null,
        parkingId = null,
        sector,
        latitude,
        longitude,
    )
}
