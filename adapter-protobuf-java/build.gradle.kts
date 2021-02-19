import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.google.protobuf")
    `java-library`
}

project.description = "Defines protobuf messages used in api"

dependencies {
    api("com.google.protobuf:protobuf-java:${DependencyVersions.protobuf}")
    api("com.google.protobuf:protobuf-java-util:${DependencyVersions.protobuf}")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${DependencyVersions.protobuf}"
    }
    generateProtoTasks {
        ofSourceSet("protobuf").forEach { task ->
            task.descriptorSetOptions.includeImports = true
            task.generateDescriptorSet = true
        }
    }
}
