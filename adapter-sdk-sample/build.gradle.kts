import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    application
    idea

    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.kotlin.jvm")
    id("com.adarshr.test-logger")
    id("java")
}

val ktorVersion = "2.0.1"

dependencies {
    api(project(":adapter-sdk-ktor2-module"))

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("com.github.ajalt.clikt:clikt-jvm:3.4.2")


    testImplementation("org.slf4j:slf4j-simple:1.7.36")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

application {
    mainClass.set("com.github.oslokommune.oslonokkelen.adapter.sample.OslonokkelenAdapterSampleApplicationKt")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("keystudioctl")
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