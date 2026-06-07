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
include(":core:exercise-media")
include(":feature:routine:api")
include(":feature:routine:domain")
include(":feature:routine:data")
include(":feature:routine:impl")
include(":feature:exercise:api")
include(":feature:exercise:domain")
include(":feature:exercise:impl")
include(":feature:analysis:api")
include(":feature:analysis:domain")
include(":feature:analysis:data")
include(":feature:analysis:impl")
include(":feature:calendar:api")
include(":feature:calendar:domain")
include(":feature:calendar:data")
include(":feature:calendar:impl")
include(":feature:workout:api")
include(":feature:workout:domain")
include(":feature:workout:data")
include(":feature:workout:impl")
