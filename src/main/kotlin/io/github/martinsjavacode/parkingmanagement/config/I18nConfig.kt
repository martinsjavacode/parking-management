package io.github.martinsjavacode.parkingmanagement.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource

@Configuration
class I18nConfig {
    @Bean
    fun messageSource(): MessageSource =
        ReloadableResourceBundleMessageSource().apply {
            setBasename("classpath:messages/messages")
            setCacheSeconds(300) // Cache for 5 minutes
        }

    @Bean
    fun exceptionMessageSource(): MessageSource =
        ReloadableResourceBundleMessageSource().apply {
            setBasename("classpath:messages/exceptions")
            setCacheSeconds(300) // Cache for 5 minutes
        }
}
