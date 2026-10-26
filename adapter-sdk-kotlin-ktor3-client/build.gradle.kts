plugins {
    id("oslonokkelen.kotlin-library")
}

description = "Ktor client for fetching public keys"

dependencies {
    api(libs.nimbus.jose.jwt)

    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
}
