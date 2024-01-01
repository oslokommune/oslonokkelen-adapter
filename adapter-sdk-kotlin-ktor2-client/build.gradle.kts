import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerPlugin

plugins {
    `java-library`
    `maven-publish`
    idea

    id("org.jetbrains.kotlin.jvm")
    id("com.adarshr.test-logger")
    id("java")
}

description = "Ktor client for fetching public keys"
val ktorVersion = "2.3.7"


dependencies {
    implementation("org.slf4j:slf4j-api:2.0.9")
    api("com.nimbusds:nimbus-jose-jwt:9.37.3")

    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    testImplementation("org.slf4j:slf4j-simple:2.0.9")
    testImplementation("org.assertj:assertj-core:3.25.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

plugins.withType<TestLoggerPlugin> {
    configure<TestLoggerExtension> {
        theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA_PARALLEL
        slowThreshold = 5000
        showStackTraces = true
        showCauses = true
    }
}

tasks.test {
    useJUnitPlatform()
    reports {
        html.required.set(false)
        junitXml.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}