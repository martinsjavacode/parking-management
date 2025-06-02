package io.github.martinsjavacode.parkingmanagement.infra.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * Configuration for Redis connection and templates.
 */
@Configuration
class RedisConfig {
    /**
     * Creates a reactive Redis template for string operations.
     *
     * @param factory The reactive Redis connection factory
     * @return A configured ReactiveRedisTemplate for string operations
     */
    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        val serializationContext =
            RedisSerializationContext
                .newSerializationContext<String, String>(StringRedisSerializer())
                .build()
        return ReactiveRedisTemplate(factory, serializationContext)
    }
}
