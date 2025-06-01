package io.github.martinsjavacode.parkingmanagement

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@EnableR2dbcRepositories(basePackages = ["io.github.martinsjavacode.parkingmanagement.infra.persistence"])
class ParkingManagementApplication

fun main(args: Array<String>) {
    runApplication<ParkingManagementApplication>(*args)
}

inline fun <reified T> loggerFor(): Logger = LoggerFactory.getLogger(T::class.java)
