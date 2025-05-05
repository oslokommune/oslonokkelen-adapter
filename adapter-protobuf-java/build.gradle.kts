plugins {
    alias(libs.plugins.protobuf)
    `java-library`
    `maven-publish`
    idea
}

project.description = "Defines protobuf messages used in api"


dependencies {
    api(libs.protobuf.java)
    api(libs.protobuf.javaUtil)
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}