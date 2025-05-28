package io.github.martinsjavacode.parkingmanagement.infra.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("vehicles")
data class VehicleEntity(
    @Id  var id: Long? = null,
    @Column("license_plate")
    var licensePlate: String
)
