package io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.repository

import io.github.martinsjavacode.parkingmanagement.infra.persistence.parking.entity.ParkingEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ParkingRepository : CoroutineCrudRepository<ParkingEntity, Long>
