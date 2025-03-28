plugins {
    id("com.google.protobuf")
    `java-library`
    `maven-publish`
    idea
}

project.description = "Defines protobuf messages used in api"

val protobufVersion = "4.29.3"

dependencies {
    api("com.google.protobuf:protobuf-java:$protobufVersion")
    api("com.google.protobuf:protobuf-java-util:$protobufVersion")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    generateProtoTasks {
        ofSourceSet("protobuf").forEach { task ->
            task.descriptorSetOptions.includeImports = true
            task.generateDescriptorSet = true
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}