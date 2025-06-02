package io.github.martinsjavacode.parkingmanagement.domain.gateway.redis

/**
 * Port for distributed locking operations.
 * Defines the contract for acquiring and releasing locks for concurrent operations.
 */
interface DistributedLockPort {
    /**
     * Tries to acquire a lock for a specific parking spot.
     *
     * @param latitude The latitude of the parking spot
     * @param longitude The longitude of the parking spot
     * @param licensePlate The license plate trying to acquire the lock
     * @param timeoutSeconds The lock timeout in seconds
     * @return true if lock was acquired, false otherwise
     */
    suspend fun acquireLock(
        latitude: Double,
        longitude: Double,
        licensePlate: String,
        timeoutSeconds: Long = 30L,
    ): Boolean

    /**
     * Releases a lock for a specific parking spot.
     * Only releases if the lock is owned by the specified license plate.
     *
     * @param latitude The latitude of the parking spot
     * @param longitude The longitude of the parking spot
     * @param licensePlate The license plate that should own the lock
     * @return true if lock was released, false otherwise
     */
    suspend fun releaseLock(
        latitude: Double,
        longitude: Double,
        licensePlate: String,
    ): Boolean

    /**
     * Checks if an operation with the same parameters has already been processed.
     * Used for idempotency checks.
     *
     * @param latitude The latitude of the parking spot
     * @param longitude The longitude of the parking spot
     * @param eventId Unique identifier for the event
     * @param timeoutSeconds How long to keep the idempotency record
     * @return true if this is a new operation, false if it's a duplicate
     */
    suspend fun checkAndMarkIdempotency(
        latitude: Double,
        longitude: Double,
        eventId: String,
        // ttl default 1 day
        timeoutSeconds: Long = 86400L,
    ): Boolean

    /**
     * Releases a lock for a specific parking spot.
     * Used for idempotency ket
     *
     * @param latitude The latitude of the parking spot
     * @param longitude The longitude of the parking spot
     * @return true if lock was released, false otherwise
     */
    suspend fun releaseIdempotencyKey(
        latitude: Double,
        longitude: Double,
    ): Boolean
}
