package io.github.martinsjavacode.parkingmanagement.service.revenue

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType.*
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType.DAILY_REVENUE_NOT_FOUND
import io.github.martinsjavacode.parkingmanagement.domain.exception.RevenueNotFoundException
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingCustomQueryRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.revenue.RevenueRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.Revenue
import io.github.martinsjavacode.parkingmanagement.loggerFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class UpdateOrInitializeDailyRevenueHandler(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort,
    private val revenueRepository: RevenueRepositoryPort,
) {
    private val logger = loggerFor<UpdateOrInitializeDailyRevenueHandler>()
    private val currencyDate = LocalDate.now()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    @Transactional
    suspend fun handle(
        eventType: EventType,
        latitude: Double,
        longitude: Double,
        amountPaid: BigDecimal = BigDecimal.ZERO,
    ): Revenue? {
        // GET PARKING STARTING COORDINATES
        logger.info("Getting parking starting coordinates")
        val parking = parkingCustomQueryRepository.findParkingByCoordinates(latitude, longitude)

        return parking?.id?.let { parkingId ->
            when (eventType) {
                PARKED -> initializeDailyRevenue(parkingId)
                EXIT -> updateDailyRevenue(parkingId, amountPaid)
                else -> null
            }
        }
    }

    private suspend fun initializeDailyRevenue(parkingId: Long): Revenue? {
        var revenueFound =
            withContext(dispatcherIO) {
                revenueRepository.getRevenueForParkingOnDate(parkingId, currencyDate)
            }

        if (revenueFound == null) {
            val newRevenue =
                Revenue(
                    parkingId = parkingId,
                    date = LocalDate.now(),
                    currency = CurrencyType.BRL,
                    amount = BigDecimal.ZERO,
                )
            revenueFound = revenueRepository.upsert(newRevenue)
        }

        return revenueFound
    }

    private suspend fun updateDailyRevenue(
        parkingId: Long,
        amountPaid: BigDecimal,
    ): Revenue {
        val revenueFound =
            withContext(dispatcherIO) {
                revenueRepository.getRevenueForParkingOnDate(parkingId, currencyDate)
            }

        return if (revenueFound != null) {
            val revenue = revenueFound.copy(amount = revenueFound.amount.add(amountPaid))
            withContext(dispatcherIO) {
                revenueRepository.upsert(revenue)
            }
        } else {
            val locale = LocaleContextHolder.getLocale()
            throw RevenueNotFoundException(
                DAILY_REVENUE_NOT_FOUND.code(),
                messageSource.getMessage(
                    DAILY_REVENUE_NOT_FOUND.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${DAILY_REVENUE_NOT_FOUND.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.PERSISTENCE_REQUEST,
            )
        }
    }
}
