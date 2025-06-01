package io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEventEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param

interface ParkingEventRepository : CoroutineCrudRepository<ParkingEventEntity, Long> {
    suspend fun findByLicensePlate(licensePlate: String): Flow<ParkingEventEntity>?

    @Query(
        """
        SELECT *
        FROM parking_events
        WHERE license_plate = :license_plate
            AND event_type = :event_type
        ORDER BY entry_time DESC
        LIMIT 1
    """,
    )
    suspend fun findLastByLicensePlateAndEventType(
        @Param("licensePlate") licensePlate: String,
        @Param("eventType") eventType: EventType,
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
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
    ): ParkingEventEntity
}
