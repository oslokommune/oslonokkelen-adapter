plugins {
    `java-library`
    `maven-publish`
    idea
}

group = "com.github.oslokommune.oslonokkelen.adapter"
version = "1.0.0-SNAPSHOT"

val sdkmanJavaMajorVersion: Provider<Int> =
    providers
        .fileContents(rootProject.layout.projectDirectory.file(".sdkmanrc"))
        .asText
        .map { content ->
            content
                .lines()
                .first { it.startsWith("java=") }
                .removePrefix("java=")
                .substringBefore(".")
                .toInt()
        }

java {
    toolchain {
        languageVersion = sdkmanJavaMajorVersion.map(JavaLanguageVersion::of)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
