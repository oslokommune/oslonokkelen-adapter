plugins {
    id("oslonokkelen.java-library")
    alias(libs.plugins.protobuf)
}

project.description = "Defines protobuf messages used in api"

dependencies {
    api(libs.protobuf.java)
    api(libs.protobuf.java.util)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.java.get()}"
    }
    generateProtoTasks {
        ofSourceSet("protobuf").forEach { task ->
            task.descriptorSetOptions.includeImports = true
            task.generateDescriptorSet = true
        }
    }
}
