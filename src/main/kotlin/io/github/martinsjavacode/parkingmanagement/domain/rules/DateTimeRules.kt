package io.github.martinsjavacode.parkingmanagement.domain.rules

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Object containing date and time-related rules for the system.
 *
 * This singleton object implements utility functions for handling
 * and validating dates and times in the parking system context.
 */
object DateTimeRules {
    /**
     * Converts a string to LocalTime using a specified pattern.
     * @param timeString The time string
     * @param pattern The time format pattern
     * @return LocalTime object
     */
    fun parseStringToLocalTime(
        timeString: String,
        pattern: String,
    ): LocalTime {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalTime.parse(timeString, formatter)
    }

    fun calculateElapsedTimeAsLocalTime(
        start: LocalDateTime,
        end: LocalDateTime,
    ): LocalDateTime {
        val durationInSeconds = Duration.between(start, end).seconds

        // Normalized duration to fit a day (24h)
        // Ensure the value of `secondOfDay` is within the valid range (0 to 86399)
        // by handling potential negative values of `durationInSeconds`.
        // The calculation works as follows:
        // 1. Calculate `durationInSeconds % 86400` to get the remainder when dividing by the number of seconds in a day.
        // 2. If the result is negative (e.g., -10763), add 86400 to make it positive.
        // 3. Apply `% 86400` again to ensure the value falls within the range of 0 to 86399.
        // This adjustment is necessary because the `%` operator in Kotlin can return negative values
        // when `durationInSeconds` is negative.
        val secondOfDay = ((durationInSeconds % (24 * 60 * 60)) + (24 * 60 * 60)) % (24 * 60 * 60)

        return LocalDateTime.of(LocalDate.now(), LocalTime.ofSecondOfDay(secondOfDay))
    }
}
