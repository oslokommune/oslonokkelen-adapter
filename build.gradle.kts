import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("idea")
    id("jacoco")
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    id("com.adarshr.test-logger") version "3.2.0"
    id("com.google.protobuf") version "0.9.2"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
}

allprojects {
    group = "com.github.oslokommune.oslonokkelen.adapter"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    pluginManager.withPlugin("kotlin") {
        tasks.withType<KotlinCompile> {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_18.toString()
        }
    }
    pluginManager.withPlugin("java") {
        tasks.withType<JavaCompile> {
            targetCompatibility = JavaVersion.VERSION_18.toString()
            sourceCompatibility = JavaVersion.VERSION_18.toString()
        }
    }
}

