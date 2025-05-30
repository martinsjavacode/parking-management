package io.github.martinsjavacode.parkingmanagement.infra.persistence.revenue

import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType.BRL
import io.github.martinsjavacode.parkingmanagement.domain.extension.toDomain
import io.github.martinsjavacode.parkingmanagement.domain.extension.toEntity
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.revenue.RevenueRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.Revenue
import io.github.martinsjavacode.parkingmanagement.infra.persistence.handler.PersistenceHandler
import io.github.martinsjavacode.parkingmanagement.infra.persistence.revenue.repository.RevenueRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class RevenueRepositoryAdapter(
    private val revenueRepository: RevenueRepository,
    private val persistenceHandler: PersistenceHandler,
) : RevenueRepositoryPort {
    @Transactional(propagation = Propagation.SUPPORTS)
    override suspend fun findDailyRevenueByParkingId(parkingId: Long): Revenue? {
        require(parkingId > 0) { "Parking id must be greater than 0" }

        return persistenceHandler.handleOperation {
            revenueRepository.findByParkingIdAndDateAndCurrency(
                parkingId,
                date = LocalDate.now(),
                currency = BRL,
            )?.toDomain()
        }
    }

    override suspend fun upsert(revenue: Revenue): Revenue {
        return persistenceHandler.handleOperation {
            val entity = revenue.toEntity()
            revenueRepository.save(entity).toDomain()
        }
    }
}
