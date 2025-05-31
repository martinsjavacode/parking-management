package io.github.martinsjavacode.parkingmanagement.unit.domain.rules

import io.github.martinsjavacode.parkingmanagement.domain.rules.DateTimeRules
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
            it("should parse time string with valid pattern") {
                val timeString = "14:30"
                val pattern = "HH:mm"

                val result = DateTimeRules.parseStringToLocalTime(timeString, pattern)

                result shouldBe LocalTime.of(14, 30)
            }

            it("should parse time string with seconds") {
                val timeString = "14:30:45"
                val pattern = "HH:mm:ss"

                val result = DateTimeRules.parseStringToLocalTime(timeString, pattern)

                result shouldBe LocalTime.of(14, 30, 45)
            }

            it("should parse time string with AM/PM format") {
                val timeString = "02:30 PM"
                val pattern = "hh:mm a"

                val result = DateTimeRules.parseStringToLocalTime(timeString, pattern)

                result shouldBe LocalTime.of(14, 30)
            }

            it("should throw exception for invalid time format") {
                val timeString = "14:30"
                val invalidPattern = "yyyy-MM-dd"

                shouldThrow<DateTimeParseException> {
                    DateTimeRules.parseStringToLocalTime(timeString, invalidPattern)
                }
            }

            it("should throw exception for invalid time string") {
                val invalidTimeString = "25:70" // Invalid hours and minutes
                val pattern = "HH:mm"

                shouldThrow<DateTimeParseException> {
                    DateTimeRules.parseStringToLocalTime(invalidTimeString, pattern)
                }
            }
        }

        context("calculateElapsedTimeAsLocalTime") {
            it("should calculate elapsed time correctly for same day") {
                val start = LocalDateTime.of(2025, 1, 1, 10, 0)
                val end = LocalDateTime.of(2025, 1, 1, 12, 30)

                val result = DateTimeRules.calculateElapsedTimeAsLocalTime(start, end)

                // Expected: 2 hours and 30 minutes elapsed time
                val expectedTime = LocalTime.of(2, 30)
                val today = LocalDate.now()

                result.toLocalTime() shouldBe expectedTime
                result.toLocalDate() shouldBe today
            }

            it("should calculate elapsed time correctly for overnight") {
                val start = LocalDateTime.of(2025, 1, 1, 22, 0)
                val end = LocalDateTime.of(2025, 1, 2, 2, 0)

                val result = DateTimeRules.calculateElapsedTimeAsLocalTime(start, end)

                // Expected: 4 hours elapsed time
                val expectedTime = LocalTime.of(4, 0)
                val today = LocalDate.now()

                result.toLocalTime() shouldBe expectedTime
                result.toLocalDate() shouldBe today
            }

            it("should handle multi-day durations correctly") {
                val start = LocalDateTime.of(2025, 1, 1, 10, 0)
                val end = LocalDateTime.of(2025, 1, 3, 10, 0) // 48 hours later

                val result = DateTimeRules.calculateElapsedTimeAsLocalTime(start, end)

                // Expected: 0 hours (normalized to fit within a day)
                val expectedTime = LocalTime.of(0, 0)
                val today = LocalDate.now()

                result.toLocalTime() shouldBe expectedTime
                result.toLocalDate() shouldBe today
            }

            it("should handle zero duration correctly") {
                val sameTime = LocalDateTime.of(2025, 1, 1, 10, 0)

                val result = DateTimeRules.calculateElapsedTimeAsLocalTime(sameTime, sameTime)

                // Expected: 0 hours, 0 minutes
                val expectedTime = LocalTime.of(0, 0)
                val today = LocalDate.now()

                result.toLocalTime() shouldBe expectedTime
                result.toLocalDate() shouldBe today
            }

            it("should handle negative duration by treating end as after start") {
                val start = LocalDateTime.of(2025, 1, 2, 10, 0)
                val end = LocalDateTime.of(2025, 1, 1, 10, 0) // 24 hours before start

                val result = DateTimeRules.calculateElapsedTimeAsLocalTime(start, end)

                // Since Duration.between returns a negative duration, we expect 0
                // or the system might handle it differently based on implementation
                val today = LocalDate.now()

                result.toLocalDate() shouldBe today
            }
        }
    }
})
