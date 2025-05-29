package io.github.martinsjavacode.parkingmanagement.infra.persistence.entity

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("parking_events")
data class ParkingEventEntity(
    @Id var id: Long? = null,
    @Column("license_plate")
    var licensePlate: String,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    @Column("entry_time")
    var entryTime: LocalDateTime,
    @Column("exit_time")
    var exitTime: LocalDateTime? = null,
    @Column("event_type")
    var eventType: EventType,
)
