import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.google.protobuf")
    `java-library`
    maven
    idea
}

project.description = "Defines protobuf messages used in api"

dependencies {
    api("com.google.protobuf:protobuf-java:3.15.3")
    api("com.google.protobuf:protobuf-java-util:3.15.3")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.15.4"
    }
    generateProtoTasks {
        ofSourceSet("protobuf").forEach { task ->
            task.descriptorSetOptions.includeImports = true
            task.generateDescriptorSet = true
        }
    }
}
