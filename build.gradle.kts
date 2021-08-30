plugins {
    id("idea")
    id("jacoco")
    id("org.jetbrains.kotlin.jvm") version "1.5.30"
    id("com.adarshr.test-logger") version "3.0.0"
    id("com.google.protobuf") version "0.8.17"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
}

allprojects {
    group = "com.github.oslokommune.oslonokkelen.adapter"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
