package io.github.martinsjavacode.parkingmanagement.infra.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("parking_spots")
data class ParkingSpotEntity(
    @Id
    var id: Long?,
    @Column("parking_id")
    var parkingId: Long,
    var latitude: Double,
    var longitude: Double,
)
