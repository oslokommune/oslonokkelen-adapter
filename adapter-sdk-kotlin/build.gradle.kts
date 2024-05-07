import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerPlugin
import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    `java-library`
    `maven-publish`
    idea

    id("org.jetbrains.kotlin.jvm")
    id("com.adarshr.test-logger")
    id("java")
}

description = "Oslonøkkelen Adapter Kotlin SDK"

val slf4jVersion = "2.0.13"

dependencies {
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    api(project(":adapter-protobuf-java"))
    api("com.nimbusds:nimbus-jose-jwt:9.37.3")
    api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")

    testImplementation("org.slf4j:slf4j-simple:$slf4jVersion")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

plugins.withType<TestLoggerPlugin> {
    configure<TestLoggerExtension> {
        theme = ThemeType.MOCHA_PARALLEL
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