package io.github.martinsjavacode.parkingmanagement.adapters.outbound.persistence

import io.github.martinsjavacode.parkingmanagement.domain.enums.CurrencyType
import io.github.martinsjavacode.parkingmanagement.domain.model.revenue.Revenue
import io.github.martinsjavacode.parkingmanagement.infra.config.TraceContext
import io.github.martinsjavacode.parkingmanagement.infra.exception.RevenueSaveFailedException
import io.github.martinsjavacode.parkingmanagement.infra.persistence.revenue.entity.RevenueEntity
import io.github.martinsjavacode.parkingmanagement.infra.persistence.revenue.repository.RevenueRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.LocalDate

class RevenueRepositoryAdapterTest : BehaviorSpec({
    val messageSource = mockk<MessageSource>(relaxed = true)
    val traceContext = mockk<TraceContext>(relaxed = true)
    val revenueRepository = mockk<RevenueRepository>()

    val revenueRepositoryAdapter =
        RevenueRepositoryAdapter(
            messageSource = messageSource,
            traceContext = traceContext,
            revenueRepository = revenueRepository,
        )

    beforeTest {
        clearAllMocks()

        // Default mocks for all tests
        coEvery { traceContext.traceId() } returns "test-trace-id"
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    given("A revenue entity") {
        val parkingId = 1L
        val date = LocalDate.of(2025, 1, 1)
        val amount = BigDecimal("150.00")
        val currency = CurrencyType.BRL

        val revenueEntity =
            RevenueEntity(
                id = 1L,
                parkingId = parkingId,
                date = date,
                amount = amount,
                currency = currency,
            )

        val revenue =
            Revenue(
                id = 1L,
                parkingId = parkingId,
                date = date,
                amount = amount,
                currency = currency,
            )

        `when`("Getting revenue for a parking on a specific date successfully") {
            beforeTest {
                coEvery {
                    revenueRepository.findByParkingIdAndDateAndCurrency(parkingId, date, CurrencyType.BRL)
                } returns revenueEntity
            }

            then("Should return the revenue") {
                val result = revenueRepositoryAdapter.getRevenueForParkingOnDate(parkingId, date)

                result shouldBe revenue

                coVerify(exactly = 1) {
                    revenueRepository.findByParkingIdAndDateAndCurrency(parkingId, date, CurrencyType.BRL)
                }
            }
        }

        `when`("Getting revenue for a parking on a specific date returns null") {
            beforeTest {
                coEvery {
                    revenueRepository.findByParkingIdAndDateAndCurrency(parkingId, date, CurrencyType.BRL)
                } returns null
            }

            then("Should return null") {
                val result = revenueRepositoryAdapter.getRevenueForParkingOnDate(parkingId, date)

                result shouldBe null

                coVerify(exactly = 1) {
                    revenueRepository.findByParkingIdAndDateAndCurrency(parkingId, date, CurrencyType.BRL)
                }
            }
        }

        `when`("Getting revenue with invalid parking ID") {
            then("Should throw IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    revenueRepositoryAdapter.getRevenueForParkingOnDate(0, date)
                }

                coVerify(exactly = 0) {
                    revenueRepository.findByParkingIdAndDateAndCurrency(any(), any(), any())
                }
            }
        }

        `when`("Upserting a revenue successfully") {
            val newRevenue =
                Revenue(
                    id = null,
                    parkingId = parkingId,
                    date = date,
                    amount = amount,
                    currency = currency,
                )

            beforeTest {
                coEvery { revenueRepository.save(any()) } returns revenueEntity
            }

            then("Should return the saved revenue") {
                val result = revenueRepositoryAdapter.upsert(newRevenue)

                result shouldBe revenue

                coVerify(exactly = 1) { revenueRepository.save(any()) }
            }
        }

        `when`("Upserting a revenue fails") {
            val newRevenue =
                Revenue(
                    id = null,
                    parkingId = parkingId,
                    date = date,
                    amount = amount,
                    currency = currency,
                )

            beforeTest {
                coEvery { revenueRepository.save(any()) } throws RuntimeException("Database error")
            }

            then("Should throw RevenueSaveFailedException") {
                shouldThrow<RevenueSaveFailedException> {
                    revenueRepositoryAdapter.upsert(newRevenue)
                }

                coVerify(exactly = 1) { revenueRepository.save(any()) }
            }
        }
    }
})
