package io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.impl

import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingCapacityAndOccupancy
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEntity
import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository.ParkingCustomQueryRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalTime

@Component
class ParkingCustomQueryRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate,
) : ParkingCustomQueryRepository {
    override suspend fun findParkingByLatitudeAndLongitude(
        latitude: Double,
        longitude: Double,
    ): ParkingEntity? {
        val query = """
            SELECT p.*
            FROM parking p
            INNER JOIN parking_spots ps ON p.id = ps.parking_id
            WHERE ps.latitude = :latitude AND ps.longitude = :longitude;
        """
        return entityTemplate.databaseClient
            .sql(query)
            .bind("latitude", latitude)
            .bind("longitude", longitude)
            .map { row, metadata ->
                ParkingEntity(
                    id = row.get("id", Long::class.java)!!,
                    sector = row.get("sector_name", String::class.java)!!,
                    basePrice = row.get("base_price", BigDecimal::class.java)!!,
                    maxCapacity = row.get("max_capacity", Int::class.java)!!,
                    openHour = row.get("open_hour", LocalTime::class.java)!!,
                    closeHour = row.get("close_hour", LocalTime::class.java)!!,
                    durationLimitMinutes = row.get("duration_limit_minutes", Int::class.java)!!,
                )
            }.awaitSingleOrNull()
    }

    override suspend fun findParkingCapacityAndOccupancy(
        latitude: Double,
        longitude: Double,
    ): ParkingCapacityAndOccupancy? {
        val query = """
            SELECT p.max_capacity,
            COALESCE(COUNT(pe.id) FILTER (WHERE pe.event_type != 'EXIT'), 0) AS spot_occupancy
            FROM parking p
                 INNER JOIN parking_spots ps ON ps.parking_id = p.id
                 LEFT JOIN parking_events pe
                    ON ps.latitude = pe.latitude
                    AND ps.longitude = pe.longitude
                    AND DATE(pe.entry_time) = DATE(now())
            WHERE ps.latitude = :latitude AND ps.longitude = :longitude
            GROUP BY p.max_capacity;
        """
        return entityTemplate.databaseClient
            .sql(query)
            .bind("latitude", latitude)
            .bind("longitude", longitude)
            .map { row, metadata ->
                ParkingCapacityAndOccupancy(
                    maxCapacity = row.get("max_capacity", Int::class.java)!!,
                    spotOccupancy = row.get("spot_occupancy", Int::class.java)!!,
                )
            }.awaitSingleOrNull()
    }
}
