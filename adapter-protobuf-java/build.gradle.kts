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

val protobufVersion = "3.15.7"

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
