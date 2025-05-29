package io.github.martinsjavacode.parkingmanagement.infra.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalTime

@Table("parking")
class ParkingEntity(
    @Id
    var id: Long?,
    @Column("sector_name")
    var sector: String,
    @Column("base_price")
    var basePrice: Double,
    @Column("max_capacity")
    var maxCapacity: Int,
    @Column("open_hour")
    var openHour: LocalTime,
    @Column("close_hour")
    var closeHour: LocalTime,
    @Column("duration_limit_minutes")
    var durationLimitMinutes: Int,
)
