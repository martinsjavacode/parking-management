package io.github.martinsjavacode.parkingmanagement.infra.persistence.repository

import io.github.martinsjavacode.parkingmanagement.infra.persistence.entity.ParkingEventEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ParkingEventRepository : CoroutineCrudRepository<ParkingEventEntity, Long> {
    suspend fun findByLicensePlate(licensePlate: String): ParkingEventEntity?
}
