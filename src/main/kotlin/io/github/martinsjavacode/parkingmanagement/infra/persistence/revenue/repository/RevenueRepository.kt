package io.github.martinsjavacode.parkingmanagement.infra.persistence.revenue.repository

import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import io.github.martinsjavacode.parkingmanagement.infra.persistence.revenue.entity.RevenueEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface RevenueRepository : CoroutineCrudRepository<RevenueEntity, Long> {
    suspend fun findByParkingIdAndDateAndCurrency(
        parkingId: Long,
        date: LocalDate,
        currency: CurrencyType,
    ): RevenueEntity?
}
