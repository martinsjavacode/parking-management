package io.github.martinsjavacode.parkingmanagement.infra.persistence.repository

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.infra.persistence.entity.ParkingEventEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param

interface ParkingEventRepository : CoroutineCrudRepository<ParkingEventEntity, Long> {
    suspend fun findByLicensePlate(licensePlate: String): ParkingEventEntity?
    suspend fun findByLicensePlateAndEventTypeNot(licensePlate: String, eventTypes: EventType): ParkingEventEntity?

    @Query(
        """
        SELECT * FROM parking_events
        WHERE (license_plate = :licensePlate OR (latitude = :latitude AND longitude = :longitude))
        AND event_type != :eventType
        """
    )
    suspend fun findParkingEventsByLicensePlateOrCoordinatesAndEventTypeNot(
        @Param("licensePlate") licensePlate: String,
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("eventType") eventType: EventType
    ): Flow<ParkingEventEntity>
}
