plugins {
    id("idea")
    id("jacoco")
    id("org.jetbrains.kotlin.jvm") version "1.4.31"
    id("com.adarshr.test-logger") version "2.1.1"
    id("com.google.protobuf") version "0.8.15"
    id("org.owasp.dependencycheck") version "6.1.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

allprojects {
    group = "com.github.oslokommune.oslonokkelen.adapter"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter() // Try to get rid of this
    }
}
