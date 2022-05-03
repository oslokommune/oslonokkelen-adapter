plugins {
    `java-library`
    `maven-publish`
    idea

    id("org.jetbrains.kotlin.jvm")
    id("com.adarshr.test-logger")
    id("java")
}

val ktorVersion = "2.0.1"

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.36")
    api(project(":adapter-sdk-ktor2-module"))

    testImplementation("org.slf4j:slf4j-simple:1.7.36")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
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