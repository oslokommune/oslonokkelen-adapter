import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("idea")
    id("jacoco")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.testLogger)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.ktlint)
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
