package io.github.martinsjavacode.parkingmanagement.domain.gateway.repository.parking

import io.github.martinsjavacode.parkingmanagement.domain.enums.EventType
import io.github.martinsjavacode.parkingmanagement.domain.model.parking.ParkingEvent
import kotlinx.coroutines.flow.Flow

/**
 * Port interface for parking event repository operations.
 *
 * This interface defines the contract for repository operations related to parking event entities,
 * following the hexagonal architecture pattern.
 */
interface ParkingEventRepositoryPort {
    /**
     * Saves a parking event entity.
     *
     * @param parkingEvent The parking event to be saved
     */
    suspend fun save(parkingEvent: ParkingEvent)

    /**
     * Finds all parking events for a specific license plate.
     *
     * @param licensePlate The license plate to search for
     * @return A flow of parking events associated with the license plate
     */
    suspend fun findAllByLicensePlate(licensePlate: String): Flow<ParkingEvent>

    /**
     * Finds the most recent parking event for a specific license plate and event type.
     *
     * @param licensePlate The license plate to search for
     * @param eventType The event type to filter by
     * @return The most recent parking event matching the criteria
     * @throws ParkingEventNotFoundException if no matching event is found
     */
    suspend fun findLastParkingEventByLicenseAndEventType(
        licensePlate: String,
        eventType: EventType,
    ): ParkingEvent

    /**
     * Finds the most recent parking event at specific coordinates.
     *
     * @param latitude The latitude coordinate to search for
     * @param longitude The longitude coordinate to search for
     * @return The most recent parking event at the specified coordinates
     * @throws ParkingEventNotFoundException if no matching event is found
     */
    suspend fun findMostRecentByCoordinates(
        latitude: Double,
        longitude: Double,
    ): ParkingEvent
}
