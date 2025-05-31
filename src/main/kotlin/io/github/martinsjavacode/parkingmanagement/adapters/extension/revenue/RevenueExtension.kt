package io.github.martinsjavacode.parkingmanagement.adapters.extension.revenue

import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import io.github.martinsjavacode.parkingmanagement.infra.persistence.revenue.entity.RevenueEntity

fun Revenue.toEntity() =
    RevenueEntity(
        id = this.id,
        parkingId = this.parkingId,
        date = this.date,
        amount = this.amount,
        currency = this.currency,
    )

fun RevenueEntity.toDomain() =
    Revenue(
        id = this.id,
        parkingId = this.parkingId,
        date = this.date,
        amount = this.amount,
        currency = this.currency,
    )
