package io.github.martinsjavacode.parkingmanagement.adapters.outbound.redis

import io.github.martinsjavacode.parkingmanagement.domain.gateway.redis.DistributedLockPort
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Redis implementation of the DistributedLockPort.
 * Provides distributed locking and idempotency mechanisms using Redis.
 */
@Component
class RedisDistributedLockAdapter(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
) : DistributedLockPort {
    companion object {
        private const val LOCK_PREFIX = "parking:lock:"
        private const val IDEMPOTENCY_PREFIX = "parking:idempotency:"
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun acquireLock(
        latitude: Double,
        longitude: Double,
        licensePlate: String,
        timeoutSeconds: Long,
    ): Boolean {
        val lockKey = generateLockKey(latitude, longitude)
        return reactiveRedisTemplate.opsForValue()
            .setIfAbsent(
                lockKey,
                licensePlate,
                Duration.ofSeconds(timeoutSeconds),
            )
            .awaitFirst()
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun releaseLock(
        latitude: Double,
        longitude: Double,
        licensePlate: String,
    ): Boolean {
        val lockKey = generateLockKey(latitude, longitude)
        val currentOwner =
            reactiveRedisTemplate.opsForValue()
                .get(lockKey)
                .awaitFirstOrNull()

        if (currentOwner == licensePlate) {
            return reactiveRedisTemplate.opsForValue()
                .delete(lockKey)
                .awaitFirst()
        }
        return false
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun checkAndMarkIdempotency(
        latitude: Double,
        longitude: Double,
        eventId: String,
        timeoutSeconds: Long,
    ): Boolean {
        val idempotencyKey = generateIdempotencyKey(latitude, longitude)
        return reactiveRedisTemplate.opsForValue()
            .setIfAbsent(idempotencyKey, eventId, Duration.ofSeconds(timeoutSeconds))
            .awaitFirst()
    }

    override suspend fun releaseIdempotencyKey(
        latitude: Double,
        longitude: Double,
    ): Boolean {
        val idempotencyKey = generateIdempotencyKey(latitude, longitude)
        return reactiveRedisTemplate.opsForValue()
            .delete(idempotencyKey)
            .awaitFirst()
    }

    private fun generateLockKey(
        latitude: Double,
        longitude: Double,
    ): String {
        return "$LOCK_PREFIX${latitude}_$longitude"
    }

    private fun generateIdempotencyKey(
        latitude: Double,
        longitude: Double,
    ): String {
        return "$IDEMPOTENCY_PREFIX${latitude}_$longitude"
    }
}
