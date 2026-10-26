rootProject.name = "build-logic"

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        exclusiveContent {
            forRepository { gradlePluginPortal() }
            filter {
                includeGroupAndSubgroups("com.adarshr")
                includeGroupAndSubgroups("org.jlleitschuh.gradle")
            }
        }
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
