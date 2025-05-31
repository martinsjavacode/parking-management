package io.github.martinsjavacode.parkingmanagement.service.revenue.impl

import io.github.martinsjavacode.parkingmanagement.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType
import io.github.martinsjavacode.parkingmanagement.domain.exception.RevenueNotFoundException
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking.ParkingRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.revenue.RevenueRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.Revenue
import io.github.martinsjavacode.parkingmanagement.loggerFor
import io.github.martinsjavacode.parkingmanagement.service.revenue.GetDailyBillingByParkingSectorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class GetDailyBillingByParkingSectorHandlerImpl(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingRepository: ParkingRepositoryPort,
    private val revenueRepository: RevenueRepositoryPort,
) : GetDailyBillingByParkingSectorHandler {
    private val logger = loggerFor<GetDailyBillingByParkingSectorHandlerImpl>()
    private val locale = LocaleContextHolder.getLocale()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcherIO = Dispatchers.IO.limitedParallelism(10)

    override suspend fun handle(
        date: LocalDate,
        sectorName: String,
    ): Revenue {
        val parking =
            withContext(dispatcherIO) {
                parkingRepository.findBySectorName(sectorName)
            }

        return runCatching {
            withContext(dispatcherIO) {
                revenueRepository.getRevenueForParkingOnDate(parking.id!!, date)
            }
        }.getOrNull() ?: throw RevenueNotFoundException(
            InternalCodeType.REVENUE_NOT_FOUND.code(),
            messageSource.getMessage(
                InternalCodeType.REVENUE_NOT_FOUND.messageKey(),
                arrayOf(parking.id, date),
                locale,
            ),
            messageSource.getMessage(
                "${InternalCodeType.REVENUE_NOT_FOUND.messageKey()}.friendly",
                null,
                locale,
            ),
            traceContext.traceId(),
            ExceptionType.PERSISTENCE_REQUEST,
        )
    }
}
