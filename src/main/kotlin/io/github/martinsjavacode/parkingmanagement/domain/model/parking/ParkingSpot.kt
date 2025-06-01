package io.github.martinsjavacode.parkingmanagement.domain.model.parking

/**
 * Represents an individual parking spot.
 *
 * This class contains information about the geographical location of the spot
 * and its association with a specific parking lot.
 *
 * @property id Unique identifier of the parking spot, nullable for new instances
 * @property parkingId Identifier of the parking lot this spot belongs to
 * @property latitude Latitude coordinate of the spot
 * @property longitude Longitude coordinate of the spot
 */
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
