import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerPlugin
import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    idea

    id("org.jetbrains.kotlin.jvm")
    id("com.adarshr.test-logger")
    id("java")
}

description = "Oslonøkkelen Adapter Kotlin SDK"

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.31")
    api(project(":adapter-protobuf-java"))
    api("com.nimbusds:nimbus-jose-jwt:9.10")
    api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.4")

    testImplementation("org.slf4j:slf4j-simple:1.7.30")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
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
        html.isEnabled = true
        junitXml.isEnabled = true
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "9"
    kotlinOptions.allWarningsAsErrors = false
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}