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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SmartTrainner"

include(":app")
include(":core:model")
include(":core:domain")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:data")
include(":core:designsystem")
include(":core:testing")
include(":core:ui")
include(":feature:training:api")
include(":feature:training:entry")
include(":feature:training:impl")
include(":feature:routine:api")
include(":feature:routine:entry")
include(":feature:routine:impl")
include(":feature:exercise:api")
include(":feature:exercise:entry")
include(":feature:exercise:impl")
include(":feature:analysis:api")
include(":feature:analysis:entry")
include(":feature:analysis:impl")
include(":feature:workout:api")
include(":feature:workout:entry")
include(":feature:workout:impl")
