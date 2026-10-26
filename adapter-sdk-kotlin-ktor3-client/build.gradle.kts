plugins {
    id("oslonokkelen.kotlin-library")
}

description = "Ktor client for fetching public keys"

dependencies {
    api(libs.jwt)

    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.jvm)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.kotlinx.json)
}
