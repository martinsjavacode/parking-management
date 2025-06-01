plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.springdoc.openapi-gradle-plugin")
    jacoco
}

group = "io.github.martinsjavacode"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Dependencies
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotlin Dependencies
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Fault Tolerance Dependencies
    implementation("io.github.resilience4j:resilience4j-kotlin:${properties["resilience4jVersion"]}")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:${properties["resilience4jVersion"]}")

    // Database Dependencies
    implementation("org.flywaydb:flyway-database-postgresql:${properties["flywayVersion"]}")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")

    // H2 Database for Testing
    testImplementation("com.h2database:h2")
    testImplementation("io.r2dbc:r2dbc-h2")

    // Development Dependencies
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.springframework.boot:spring-boot-docker-compose")

    // Documentation Dependencies
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:${properties["springdocVersion"]}")

    // Test Dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("com.ninja-squad:springmockk:${properties["mockkVersion"]}")
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("io.kotest:kotest-property:5.6.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "${properties["jacocoVersion"]}"
    reportsDirectory = layout.buildDirectory.dir("reports/jacoco")
}

val configurableFileTrees =
    fileTree(
        layout.buildDirectory.dir("classes/kotlin/main"),
    ).exclude(
        listOf(
            "**/config/*",
            "**/infra/*",
            "**/model/*",
            "**/enums/*",
            "**/gateway/*",
            "**/request/*",
            "**/response/*",
            "**/ParkingManagementApplication*",
        ),
    )

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)

        html.outputLocation.set(layout.buildDirectory.dir("jacoco/tests/html"))
    }

    classDirectories.setFrom(configurableFileTrees)

    dependsOn(tasks.test)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = BigDecimal.valueOf(.9)
            }
        }
    }

    classDirectories.setFrom(configurableFileTrees)

    dependsOn(tasks.jacocoTestReport)
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
