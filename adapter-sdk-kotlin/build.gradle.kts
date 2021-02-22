import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerPlugin
import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("com.adarshr.test-logger")
    id("java")
}

description = "Oslonøkkelen Adapter Kotlin SDK"

dependencies {
    implementation("org.slf4j:slf4j-api:${DependencyVersions.slf4j}")
    implementation(project(":adapter-protobuf-java"))
    api("com.nimbusds:nimbus-jose-jwt:${DependencyVersions.joseJwt}")

    testImplementation("org.slf4j:slf4j-simple:${DependencyVersions.slf4j}")
    testImplementation("org.assertj:assertj-core:${DependencyVersions.assertj}")
    testImplementation("org.assertj:assertj-core:${DependencyVersions.assertj}")
    testImplementation("org.junit.jupiter:junit-jupiter:${DependencyVersions.junit5}")
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
    kotlinOptions.freeCompilerArgs += "-java-parameters"
    kotlinOptions.allWarningsAsErrors = false
}