plugins {
    id("oslonokkelen.kotlin-library")
}

description = "Oslonøkkelen Adapter Kotlin SDK"

dependencies {
    api(projects.adapterProtobufJava)
    api(libs.jwt)
    api(libs.kotlinx.collections)
}
