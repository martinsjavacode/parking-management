package io.github.martinsjavacode.parkingmanagement.infra.persistence.repository

import io.github.martinsjavacode.parkingmanagement.infra.persistence.entity.ParkingEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ParkingRepository : CoroutineCrudRepository<ParkingEntity, Long>
