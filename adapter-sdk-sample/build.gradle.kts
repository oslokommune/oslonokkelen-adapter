import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    application
    idea

    kotlin("plugin.serialization") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.kotlin.jvm")
    id("com.adarshr.test-logger")
    id("java")
}

val ktorVersion = "2.0.1"

dependencies {
    implementation("org.slf4j:slf4j-simple:1.7.36")

    implementation(project(":adapter-sdk-ktor2-module"))
    implementation(project(":adapter-sdk-kotlin"))
    implementation(project(":adapter-sdk-kotlin-ktor2-client"))

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("com.github.ajalt.clikt:clikt-jvm:3.4.2")

    // Ktor client
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Ktor server
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-compression:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")

    testImplementation("org.slf4j:slf4j-simple:1.7.36")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

application {
    mainClass.set("com.github.oslokommune.oslonokkelen.adapter.sample.OslonokkelenAdapterSampleApplicationKt")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("oslonokkelen-sample-adapter")
        archiveClassifier.set("")
        archiveVersion.set("")

        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "com.github.oslokommune.oslonokkelen.adapter.sample.OslonokkelenAdapterSampleApplicationKt"))
        }
    }
}


plugins.withType<com.adarshr.gradle.testlogger.TestLoggerPlugin> {
    configure<com.adarshr.gradle.testlogger.TestLoggerExtension> {
        theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA_PARALLEL
        slowThreshold = 5000
        showStackTraces = true
        showCauses = true
    }
}

tasks.test {
    useJUnitPlatform()
    reports {
        html.required.set(false)
        junitXml.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}