package io.github.martinsjavacode.parkingmanagement.application.usecases.plate

import io.github.martinsjavacode.parkingmanagement.domain.model.vehicle.PlateStatus

/**
 * Interface for retrieving the status of a vehicle by license plate.
 *
 * Defines the contract for checking the current status of a vehicle
 * in the parking lot based on its license plate.
 */
interface GetPlateStatusHandler {
    /**
     * Gets the status of a vehicle based on the provided license plate.
     *
     * @param licensePlate Vehicle's license plate
     * @return The status of the vehicle
     * @throws LicensePlateNotFoundException If no vehicle with the provided license plate is found
     */
    suspend fun handle(licensePlate: String): PlateStatus
}
