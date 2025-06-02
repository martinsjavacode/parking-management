package io.github.martinsjavacode.parkingmanagement.domain.rules

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

class DateTimeRulesTest : DescribeSpec({

    describe("DateTimeRules") {

        context("parseStringToLocalTime") {
            it("should parse time string with HH:mm pattern") {
                // Given
                val timeString = "14:30"
                val pattern = "HH:mm"

                // When
                val result = DateTimeRules.parseStringToLocalTime(timeString, pattern)

                // Then
                result shouldBe LocalTime.of(14, 30)
            }

            it("should parse time string with HH:mm:ss pattern") {
                // Given
                val timeString = "14:30:45"
                val pattern = "HH:mm:ss"

                // When
                val result = DateTimeRules.parseStringToLocalTime(timeString, pattern)

                // Then
                result shouldBe LocalTime.of(14, 30, 45)
            }

            it("should parse time string with h:mm a pattern") {
                // Given
                val timeString = "2:30 PM"
                val pattern = "h:mm a"

                // When
                val result = DateTimeRules.parseStringToLocalTime(timeString, pattern)

                // Then
                result shouldBe LocalTime.of(14, 30)
            }

            it("should throw exception for invalid time format") {
                // Given
                val timeString = "14:30"
                val pattern = "HH:mm:ss"

                // When & Then
                shouldThrow<DateTimeParseException> {
                    DateTimeRules.parseStringToLocalTime(timeString, pattern)
                }
            }

            it("should throw exception for invalid time string") {
                // Given
                val timeString = "25:70"
                val pattern = "HH:mm"

                // When & Then
                shouldThrow<DateTimeParseException> {
                    DateTimeRules.parseStringToLocalTime(timeString, pattern)
                }
            }
        }

        context("calculateElapsedTimeAsLocalTime") {
            it("should calculate elapsed time within same day") {
                // Given
                val start = LocalDateTime.of(2025, 1, 1, 10, 0)
                val end = LocalDateTime.of(2025, 1, 1, 14, 30)

                // When
                val result = DateTimeRules.calculateElapsedTimeAsLocalTime(start, end)

                // Then
                val expectedTime = LocalTime.of(4, 30)
                val expectedDate = LocalDate.now()
                result shouldBe LocalDateTime.of(expectedDate, expectedTime)
            }

            it("should calculate elapsed time across days") {
                // Given
                val start = LocalDateTime.of(2025, 1, 1, 22, 0)
                val end = LocalDateTime.of(2025, 1, 2, 2, 0)

                // When
                val result = DateTimeRules.calculateElapsedTimeAsLocalTime(start, end)

                // Then
                val expectedTime = LocalTime.of(4, 0)
                val expectedDate = LocalDate.now()
                result shouldBe LocalDateTime.of(expectedDate, expectedTime)
            }

            it("should handle zero elapsed time") {
                // Given
                val dateTime = LocalDateTime.of(2025, 1, 1, 10, 0)

                // When
                val result = DateTimeRules.calculateElapsedTimeAsLocalTime(dateTime, dateTime)

                // Then
                val expectedTime = LocalTime.of(0, 0)
                val expectedDate = LocalDate.now()
                result shouldBe LocalDateTime.of(expectedDate, expectedTime)
            }

            it("should handle negative elapsed time") {
                // Given
                val start = LocalDateTime.of(2025, 1, 2, 10, 0)
                val end = LocalDateTime.of(2025, 1, 1, 8, 0) // End is before start

                // When
                val result = DateTimeRules.calculateElapsedTimeAsLocalTime(start, end)

                // Then
                // Expected: 24h - 2h = 22h
                val expectedTime = LocalTime.of(22, 0)
                val expectedDate = LocalDate.now()
                result shouldBe LocalDateTime.of(expectedDate, expectedTime)
            }

            it("should handle elapsed time exactly 24 hours") {
                // Given
                val start = LocalDateTime.of(2025, 1, 1, 10, 0)
                val end = LocalDateTime.of(2025, 1, 2, 10, 0)

                // When
                val result = DateTimeRules.calculateElapsedTimeAsLocalTime(start, end)

                // Then
                val expectedTime = LocalTime.of(0, 0)
                val expectedDate = LocalDate.now()
                result shouldBe LocalDateTime.of(expectedDate, expectedTime)
            }

            it("should handle elapsed time more than 24 hours") {
                // Given
                val start = LocalDateTime.of(2025, 1, 1, 10, 0)
                val end = LocalDateTime.of(2025, 1, 3, 14, 30) // 52 hours and 30 minutes

                // When
                val result = DateTimeRules.calculateElapsedTimeAsLocalTime(start, end)

                // Then
                // Expected: 52h 30m % 24h = 4h 30m
                val expectedTime = LocalTime.of(4, 30)
                val expectedDate = LocalDate.now()
                result shouldBe LocalDateTime.of(expectedDate, expectedTime)
            }
        }
    }
})
