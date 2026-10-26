rootProject.name = "oslonokkelen-adapter"

include(":adapter-docs")
include(":adapter-protobuf-java")
include(":adapter-sdk-kotlin")
include(":adapter-sdk-kotlin-ktor3-client")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        exclusiveContent {
            forRepository { gradlePluginPortal() }
            filter {
                includeGroupAndSubgroups("com.adarshr")
                includeGroupAndSubgroups("com.google.protobuf")
                includeGroupAndSubgroups("org.asciidoctor")
                includeGroupAndSubgroups("org.jlleitschuh.gradle")
                includeGroupAndSubgroups("org.ysb33r")
            }
        }
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}
