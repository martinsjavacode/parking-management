pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val graalvmNativeVersion: String by settings
    val ktlintVersion: String by settings
    val openApiVersion: String by settings

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        id("org.springdoc.openapi-gradle-plugin") version openApiVersion
        jacoco
    }
}

rootProject.name = "parking-management"
