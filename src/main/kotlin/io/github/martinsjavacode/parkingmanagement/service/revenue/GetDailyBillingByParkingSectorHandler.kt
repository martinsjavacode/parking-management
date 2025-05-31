package io.github.martinsjavacode.parkingmanagement.service.revenue

import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.revenue.RevenueRepositoryPort
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class GetDailyBillingByParkingSectorHandler(
    private val revenueRepository: RevenueRepositoryPort
) {

    suspend fun handle(date: LocalDate, sectorName: String) {

//        revenueRepository.getRevenueForParkingOnDate()
    }

}
