plugins {
    id("idea")
    id("jacoco")
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("com.adarshr.test-logger") version "3.2.0"
    id("com.google.protobuf") version "0.9.2"
    id("org.jlleitschuh.gradle.ktlint") version "11.1.0"
}

allprojects {
    group = "com.github.oslokommune.oslonokkelen.adapter"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
