package io.github.martinsjavacode.parkingmanagement.infra.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("spot_events")
data class SpotEvent(
    @Id var id: Long? = null,
    @Column("parking_spot_id")
    var parkingSpotId: Long,
    @Column("license_plate")
    var licensePlate: String,
    @Column("event_type")
    var eventType: String,
    @Column("event_time")
    var eventTime: String
)
