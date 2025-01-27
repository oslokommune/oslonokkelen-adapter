import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("idea")
    id("jacoco")
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("com.adarshr.test-logger") version "4.0.0"
    id("com.google.protobuf") version "0.9.4"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

allprojects {
    group = "com.github.oslokommune.oslonokkelen.adapter"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    pluginManager.withPlugin("kotlin") {
        tasks.withType<KotlinCompile> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }
    }
    pluginManager.withPlugin("java") {
        tasks.withType<JavaCompile> {
            targetCompatibility = JavaVersion.VERSION_21.toString()
            sourceCompatibility = JavaVersion.VERSION_21.toString()
        }
    }
}

