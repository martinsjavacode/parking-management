package io.github.martinsjavacode.parkingmanagement.infra.persistence.entity

import io.github.martinsjavacode.parkingmanagement.domain.enums.EntryEventType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("entry_events")
data class EntryEvent(
    @Id var id: Long? = null,
    var licensePlate: String,
    @Column("parking_id")
    var parkingId: Long,
    @Column("entry_time")
    var entryTime: String,
    @Column("event_type")
    var entryEventType: EntryEventType
)
