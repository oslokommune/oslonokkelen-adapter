import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("idea")
    id("jacoco")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("com.adarshr.test-logger") version "4.0.0"
    id("com.google.protobuf") version "0.9.4"
    id("org.jlleitschuh.gradle.ktlint") version "12.0.3"
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

