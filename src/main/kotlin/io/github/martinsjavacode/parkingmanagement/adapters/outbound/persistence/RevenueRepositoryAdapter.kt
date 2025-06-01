package io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.adapters.extension.revenue.toDomain
import io.github.martinsjavacode.parkingmanagement.adapters.extension.revenue.toEntity
import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import io.github.martinsjavacode.parkingmanagement.domain.enums.ExceptionType
import io.github.martinsjavacode.parkingmanagement.domain.enums.InternalCodeType
import io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.revenue.RevenueRepositoryPort
import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.RevenueSaveFailedException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.revenue.repository.RevenueRepository
import io.github.martinsjavacode.parkingmanagement.loggerFor
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class RevenueRepositoryAdapter(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val revenueRepository: RevenueRepository,
) : RevenueRepositoryPort {
    private val logger = loggerFor<RevenueRepositoryAdapter>()
    private val locale = LocaleContextHolder.getLocale()

    override suspend fun getRevenueForParkingOnDate(
        parkingId: Long,
        date: LocalDate,
    ): Revenue? {
        require(parkingId > 0) { "Parking id must be greater than 0" }

        return runCatching {
            revenueRepository.findByParkingIdAndDateAndCurrency(
                parkingId,
                date,
                currency = CurrencyType.BRL,
            )?.toDomain()
        }.getOrNull()
    }

    override suspend fun upsert(revenue: Revenue): Revenue {
        return runCatching {
            val entity = revenue.toEntity()
            revenueRepository.save(entity).toDomain()
        }.onFailure {
            logger.error(
                "Failed to save revenue. Parking: ${revenue.parkingId}, Trace ID: ${traceContext.traceId()}",
                it,
            )
            throw RevenueSaveFailedException(
                InternalCodeType.REVENUE_NOT_SAVED.code(),
                messageSource.getMessage(
                    InternalCodeType.REVENUE_NOT_SAVED.messageKey(),
                    null,
                    locale,
                ),
                messageSource.getMessage(
                    "${InternalCodeType.REVENUE_NOT_SAVED.messageKey()}.friendly",
                    null,
                    locale,
                ),
                traceContext.traceId(),
                ExceptionType.PERSISTENCE_REQUEST,
            )
        }.getOrThrow()
    }
}
