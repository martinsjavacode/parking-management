package io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEventEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ParkingEventRepository : CoroutineCrudRepository<ParkingEventEntity, Long> {
    suspend fun findByLicensePlate(licensePlate: String): Flow<ParkingEventEntity>?

    suspend fun findByLicensePlateAndEventType(
        licensePlate: String,
        eventType: EventType,
    ): ParkingEventEntity

    @Query(
        """
        SELECT *
        FROM parking_events
        WHERE latitude = :latitude
            AND longitude = :longitude
        ORDER BY entry_time DESC
        LIMIT 1
    """,
    )
    suspend fun findLastByLatitudeAndLongitude(
        latitude: Double,
        longitude: Double,
    ): ParkingEventEntity
}
