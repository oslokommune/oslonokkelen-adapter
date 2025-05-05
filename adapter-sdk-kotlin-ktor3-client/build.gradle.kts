import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerPlugin

plugins {
    `java-library`
    `maven-publish`
    idea

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.testLogger)
    id("java")
}

description = "Ktor client for fetching public keys"

dependencies {
    implementation(platform(libs.slf4j.bom))
    implementation(libs.slf4j.api)
    api(libs.jwt)

    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.jvm)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.kotlinx.json)

    testImplementation(libs.slf4j.simple)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.bundles.testing)
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