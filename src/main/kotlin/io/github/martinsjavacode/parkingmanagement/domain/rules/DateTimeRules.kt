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
        val secondOfDay = durationInSeconds % (24 * 60 * 60)

        return LocalDateTime.of(LocalDate.now(), LocalTime.ofSecondOfDay(secondOfDay))
    }
}
