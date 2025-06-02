package io.github.martinsjavacode.parkingmanagement

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

/**
 * Main application class for the Parking Management System.
 * 
 * This class serves as the entry point for the Spring Boot application
 * and enables R2DBC repositories for reactive database access.
 */
@SpringBootApplication
@EnableR2dbcRepositories(basePackages = ["io.github.martinsjavacode.parkingmanagement.infra.persistence"])
class ParkingManagementApplication

/**
 * Main function that starts the Spring Boot application.
 *
 * @param args Command line arguments passed to the application
 */
fun main(args: Array<String>) {
    runApplication<ParkingManagementApplication>(*args)
}

/**
 * Utility function to create a logger for a specific class.
 *
 * @param T The class to create the logger for
 * @return A configured SLF4J Logger instance
 */
inline fun <reified T> loggerFor(): Logger = LoggerFactory.getLogger(T::class.java)
