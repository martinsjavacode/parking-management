package io.github.martinsjavacode.parkingmanagement

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ParkingManagementApplication

fun main(args: Array<String>) {
    runApplication<ParkingManagementApplication>(*args)
}
