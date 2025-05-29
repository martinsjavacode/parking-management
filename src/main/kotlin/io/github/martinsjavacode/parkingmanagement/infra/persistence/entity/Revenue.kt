package io.github.martinsjavacode.parkingmanagement.infra.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("revenues")
data class Revenue(
    @Id val id: Long? = null,
    @Column("parking_id")
    val parkingId: Long,
    val date: String,
    val amount: Double,
    val currency: String,
)
