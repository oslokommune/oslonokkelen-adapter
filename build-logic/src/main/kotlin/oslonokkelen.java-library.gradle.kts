plugins {
    `java-library`
    `maven-publish`
    idea
}

group = "com.github.oslokommune.oslonokkelen.adapter"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
