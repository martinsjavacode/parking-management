package io.github.martinsjavacode.parkingmanagement.domain.rules

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object DateTimeRules {
    /**
     * Converts a string to LocalTime using a specified pattern.
     * @param timeString The time string
     * @param pattern The time format pattern
     * @return LocalTime object
     */
    fun stringToLocalTime(
        timeString: String,
        pattern: String,
    ): LocalTime {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalTime.parse(timeString, formatter)
    }

    fun stringToLocalDateTime(dateTime: String): LocalDateTime {
        return LocalDateTime.parse(dateTime)
    }
}
