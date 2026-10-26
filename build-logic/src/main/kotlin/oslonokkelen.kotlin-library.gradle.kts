import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    id("oslonokkelen.java-library")
    id("oslonokkelen.ktlint")
    id("org.jetbrains.kotlin.jvm")
    id("com.adarshr.test-logger")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(platform(libs.findLibrary("slf4j-bom").get()))
    implementation(libs.findLibrary("slf4j-api").get())

    testImplementation(libs.findLibrary("slf4j-simple").get())
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.findBundle("testing").get())
}

testlogger {
    theme = ThemeType.MOCHA_PARALLEL
    slowThreshold = 5000
    showStackTraces = true
    showCauses = true
}

tasks.test {
    useJUnitPlatform()
    reports {
        html.required = false
        junitXml.required = true
    }
}
