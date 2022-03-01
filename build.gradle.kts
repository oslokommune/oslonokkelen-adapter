plugins {
    id("idea")
    id("jacoco")
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("com.adarshr.test-logger") version "3.2.0"
    id("com.google.protobuf") version "0.8.17"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
}

allprojects {
    group = "com.github.oslokommune.oslonokkelen.adapter"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.withType<JavaPlugin> {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(16))
                vendor.set(JvmVendorSpec.ADOPTOPENJDK)
                implementation.set(JvmImplementation.J9)
            }
        }
    }
}
