pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Portfolio Movies"
include(":app")
include(":core:network")
include(":core:designsystem")
include(":core:database")
include(":core:model")
include(":core:testing")
include(":feature:movies:impl")
include(":feature:tv:impl")
include(":feature:favorites:impl")
include(":app-ui")
