package io.github.martinsjavacode.parkingmanagement.infra.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource

/**
 * Configuration for internationalization (i18n) support.
 *
 * This class configures the message source for internationalized messages
 * used throughout the application.
 */
@Configuration
class I18nConfig {
    /**
     * Creates and configures a message source for internationalized messages.
     *
     * @return A configured MessageSource bean
     */
    @Bean
    fun messageSource(): MessageSource =
        ReloadableResourceBundleMessageSource().apply {
            setBasename("classpath:messages/messages")
            setCacheSeconds(300) // Cache for 5 minutes
        }
}
