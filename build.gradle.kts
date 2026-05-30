import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ProjectDependency

plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}

val checkModuleBoundaries by tasks.registering {
    group = "verification"
    description = "Checks Smart Trainner module dependency direction rules."

    val checkedSuffixes = setOf("api", "implementation", "compileonly", "runtimeonly", "kapt", "ksp")
    val projectEdges = providers.provider {
        allprojects.flatMap { sourceProject ->
            sourceProject.configurations
                .matching { configuration ->
                    val lowerName = configuration.name.lowercase()
                    lowerName in checkedSuffixes || checkedSuffixes.any { suffix -> lowerName.endsWith(suffix) }
                }
                .flatMap { configuration ->
                    configuration.dependencies.withType(ProjectDependency::class.java).map { dependency ->
                        "${sourceProject.path}|${dependency.path}|${configuration.name}"
                    }
                }
        }
    }
    inputs.property("projectEdges", projectEdges)

    doLast {
        val implementationCoreModules = setOf(
            ":core:data",
            ":core:database",
            ":core:datastore",
            ":core:network"
        )

        fun isFeature(path: String) = path.startsWith(":feature:")
        fun isFeatureApi(path: String) = isFeature(path) && path.endsWith(":api")
        fun isFeatureEntry(path: String) = isFeature(path) && path.endsWith(":entry")
        fun isFeatureImpl(path: String) = isFeature(path) && path.endsWith(":impl")
        fun featureName(path: String) = path.split(":").getOrNull(2)

        val allowedCrossFeatureApiDependencies = setOf(
            ":feature:routine:api" to ":feature:exercise:api",
            ":feature:routine:impl" to ":feature:exercise:api",
            ":feature:workout:api" to ":feature:exercise:api",
            ":feature:workout:impl" to ":feature:exercise:api",
            ":feature:training:impl" to ":feature:analysis:api",
            ":feature:training:impl" to ":feature:exercise:api",
            ":feature:training:impl" to ":feature:routine:api",
            ":feature:training:impl" to ":feature:workout:api"
        )
        val allProjectPaths = allprojects.map { it.path }.toSet()
        val invalidAllowlistPaths = allowedCrossFeatureApiDependencies
            .flatMap { (source, target) -> listOf(source, target) }
            .filterNot { it in allProjectPaths }
            .distinct()
            .sorted()

        if (invalidAllowlistPaths.isNotEmpty()) {
            throw GradleException(
                "allowedCrossFeatureApiDependencies contains stale or invalid project paths: " +
                    invalidAllowlistPaths.joinToString()
            )
        }

        val violations = mutableListOf<String>()

        projectEdges.get().forEach { edgeString ->
            val parts = edgeString.split("|")
            val source = parts[0]
            val target = parts[1]
            val configurationName = parts[2]
            val edge = "$source -> $target ($configurationName)"

            when {
                source.startsWith(":core:") && isFeature(target) -> {
                    violations += "$edge: core modules must not depend on feature modules."
                }
                isFeature(source) && target in implementationCoreModules -> {
                    violations += "$edge: feature modules must use domain/model/UI contracts, not data/storage/network implementations."
                }
                isFeatureApi(source) && (isFeatureImpl(target) || isFeatureEntry(target)) -> {
                    violations += "$edge: feature API modules must not depend on implementation or entry modules."
                }
                isFeatureImpl(source) && (isFeatureImpl(target) || isFeatureEntry(target)) -> {
                    violations += "$edge: feature implementations must not depend on feature implementations or entries directly."
                }
                isFeatureEntry(source) && isFeature(target) && featureName(source) != featureName(target) -> {
                    violations += "$edge: feature entry modules may only bind their own feature API and implementation."
                }
                isFeature(source) &&
                    isFeatureApi(target) &&
                    featureName(source) != featureName(target) &&
                    (source to target) !in allowedCrossFeatureApiDependencies -> {
                    violations += "$edge: cross-feature API dependencies must be explicitly isolated or added to the transitional allowlist with an owner-removal plan."
                }
                source == ":app" && isFeatureImpl(target) -> {
                    violations += "$edge: app must depend on feature API/entry contracts, not feature implementations."
                }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                violations.joinToString(
                    separator = "\n",
                    prefix = "Module boundary violations:\n"
                )
            )
        }
    }
}

tasks.named("check") {
    dependsOn(checkModuleBoundaries)
}

subprojects {
    tasks.matching { it.name == "lint" || it.name == "lintDebug" }.configureEach {
        dependsOn(checkModuleBoundaries)
    }

    plugins.withId("com.android.application") {
        extensions.configure<ApplicationExtension> {
            lint {
                abortOnError = true
                warningsAsErrors = true
                checkAllWarnings = true
                checkDependencies = true
                disable += setOf(
                    "GradleDependency",
                    "AndroidGradlePluginVersion",
                    "NewerVersionAvailable"
                )
                explainIssues = true
                textReport = true
                xmlReport = true
                htmlReport = true
                sarifReport = true
            }
        }
    }

    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            lint {
                abortOnError = true
                warningsAsErrors = true
                checkAllWarnings = true
                checkDependencies = true
                disable += setOf(
                    "GradleDependency",
                    "AndroidGradlePluginVersion",
                    "NewerVersionAvailable"
                )
                explainIssues = true
                textReport = true
                xmlReport = true
                htmlReport = true
                sarifReport = true
            }
        }
    }
}
