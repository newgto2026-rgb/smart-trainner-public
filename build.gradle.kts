import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.PathSensitivity

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
    val appMainKotlinSources = layout.projectDirectory.dir("app/src/main").asFileTree.matching {
        include("**/*.kt")
    }
    val productionKotlinSources = files(
        allprojects.map { project ->
            project.layout.projectDirectory.dir("src/main").asFileTree.matching {
                include("**/*.kt")
            }
        }
    )
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
    inputs.files(appMainKotlinSources)
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.files(productionKotlinSources)
        .withPathSensitivity(PathSensitivity.RELATIVE)

    doLast {
        val deniedFeatureCoreImplementationModules = setOf(":core:data")
        val guardedCoreInfrastructureModules = setOf(
            ":core:database",
            ":core:datastore",
            ":core:network"
        )

        fun isFeature(path: String) = path.startsWith(":feature:")
        fun isFeatureApi(path: String) = isFeature(path) && path.endsWith(":api")
        fun isFeatureData(path: String) = isFeature(path) && path.endsWith(":data")
        fun isFeatureEntry(path: String) = isFeature(path) && path.endsWith(":entry")
        fun isFeatureImpl(path: String) = isFeature(path) && path.endsWith(":impl")
        fun featureName(path: String) = path.split(":").getOrNull(2)

        val allowedCrossFeatureApiDependencies = emptySet<Pair<String, String>>()
        val allowedFeaturePrivateModules = setOf(
            ":feature:routine:domain",
            ":feature:routine:data"
        )
        val allowedFeatureDataCoreInfrastructureDependencies = setOf(
            ":feature:routine:data" to ":core:database",
            ":feature:routine:data" to ":core:datastore"
        )
        val allowedAppFeatureImplDependencies = setOf(
            ":app" to ":feature:analysis:impl",
            ":app" to ":feature:exercise:impl",
            ":app" to ":feature:routine:impl",
            ":app" to ":feature:workout:impl"
        )
        val allowedAppFeatureDataDependencies = setOf(
            ":app" to ":feature:routine:data"
        )
        val allProjectPaths = allprojects.map { it.path }.toSet()
        val invalidAllowlistPaths = (
            (allowedCrossFeatureApiDependencies +
                allowedFeatureDataCoreInfrastructureDependencies +
                allowedAppFeatureImplDependencies +
                allowedAppFeatureDataDependencies)
                .flatMap { (source, target) -> listOf(source, target) } +
                allowedFeaturePrivateModules
            )
            .filterNot { it in allProjectPaths }
            .distinct()
            .sorted()

        if (invalidAllowlistPaths.isNotEmpty()) {
            throw GradleException(
                "Module boundary allowlists contain stale or invalid project paths: " +
                    invalidAllowlistPaths.joinToString()
            )
        }

        val unapprovedFeaturePrivateModules = allProjectPaths
            .filter { path ->
                val parts = path.split(":")
                parts.size == 4 &&
                    parts[1] == "feature" &&
                    parts[3] in setOf("domain", "data", "network") &&
                    path !in allowedFeaturePrivateModules
            }
            .sorted()
        if (unapprovedFeaturePrivateModules.isNotEmpty()) {
            throw GradleException(
                "Feature-local domain/data/network modules require an explicit ownership decision: " +
                    unapprovedFeaturePrivateModules.joinToString()
            )
        }

        val violations = mutableListOf<String>()
        val featureImplReferencePattern = Regex("""com\.smarttrainner\.feature\.[^.]+\.impl(\.|$)""")
        val featureDataReferencePattern = Regex("""com\.smarttrainner\.feature\.[^.]+\.data(\.|$)""")
        val coreImplementationReferencePattern =
            Regex("""com\.smarttrainner\.core\.(data|database|datastore|network)(\.|$)""")

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
                isFeature(source) && target in deniedFeatureCoreImplementationModules -> {
                    violations += "$edge: feature modules must not depend on shared data implementations."
                }
                isFeature(source) &&
                    target in guardedCoreInfrastructureModules &&
                    (source to target) !in allowedFeatureDataCoreInfrastructureDependencies -> {
                    violations += "$edge: core infrastructure is allowed only for explicitly approved feature data modules."
                }
                isFeatureApi(source) && (isFeatureImpl(target) || isFeatureEntry(target)) -> {
                    violations += "$edge: feature API modules must not depend on implementation or entry modules."
                }
                isFeatureImpl(source) && (isFeatureImpl(target) || isFeatureEntry(target)) -> {
                    violations += "$edge: feature implementations must not depend on feature implementations or entries directly."
                }
                isFeatureImpl(source) && isFeatureData(target) -> {
                    violations += "$edge: feature UI implementations must depend on domain contracts, not feature data implementations."
                }
                isFeatureData(source) &&
                    isFeature(target) &&
                    target != ":feature:${featureName(source)}:domain" -> {
                    violations += "$edge: feature data modules may depend only on their own feature domain contract."
                }
                isFeatureEntry(source) && isFeature(target) && featureName(source) != featureName(target) -> {
                    violations += "$edge: feature entry modules may only bind their own feature API and implementation."
                }
                isFeature(source) &&
                    isFeature(target) &&
                    featureName(source) != featureName(target) &&
                    (source to target) !in allowedCrossFeatureApiDependencies -> {
                    violations += "$edge: feature modules must not depend on modules from another feature; app owns cross-feature routing and composition."
                }
                source == ":app" && isFeatureEntry(target) -> {
                    violations += "$edge: app-owned DI composition should bind feature APIs directly to feature implementations; feature entry modules must not be reintroduced."
                }
                source == ":app" &&
                    isFeatureImpl(target) &&
                    (source to target) !in allowedAppFeatureImplDependencies -> {
                    violations += "$edge: app may depend on feature implementations only from the app-owned DI composition allowlist."
                }
                source == ":app" &&
                    isFeatureData(target) &&
                    (source to target) !in allowedAppFeatureDataDependencies -> {
                    violations += "$edge: app may depend on feature data only from the app-owned DI composition allowlist."
                }
            }
        }

        val appDiCoreImplementationFiles = setOf(
            "CoreRepositoryBindingsModule.kt",
            "PlatformDatabaseModule.kt",
            "PlatformNetworkModule.kt"
        )
        val appDiFeatureImplementationFiles = setOf("FeatureEntryBindingsModule.kt")
        val appDiFeatureDataFiles = setOf("RoutineDataRepositoryBindingsModule.kt")

        appMainKotlinSources.files.forEach { sourceFile ->
            val normalizedPath = sourceFile.path.replace('\\', '/')
            val isAppDiSource = "/app/src/main/java/com/smarttrainner/app/di/" in normalizedPath
            val sourceFileName = sourceFile.name

            sourceFile.useLines { lines ->
                lines.forEachIndexed { index, line ->
                    if (featureImplReferencePattern.containsMatchIn(line)) {
                        if (!isAppDiSource) {
                            violations +=
                                "${sourceFile.relativeTo(projectDir)}:${index + 1}: feature implementation references in :app are allowed only in app-owned DI composition modules."
                        } else if (sourceFileName !in appDiFeatureImplementationFiles) {
                            violations +=
                                "${sourceFile.relativeTo(projectDir)}:${index + 1}: feature implementation references in :app DI are allowed only in feature entry binding modules."
                        }
                    }
                    if (featureDataReferencePattern.containsMatchIn(line)) {
                        if (!isAppDiSource) {
                            violations +=
                                "${sourceFile.relativeTo(projectDir)}:${index + 1}: feature data references in :app are allowed only in app-owned DI composition modules."
                        } else if (sourceFileName !in appDiFeatureDataFiles) {
                            violations +=
                                "${sourceFile.relativeTo(projectDir)}:${index + 1}: feature data references in :app DI are allowed only in approved feature data repository binding modules."
                        }
                    }
                    if (coreImplementationReferencePattern.containsMatchIn(line)) {
                        if (!isAppDiSource) {
                            violations +=
                                "${sourceFile.relativeTo(projectDir)}:${index + 1}: core data/storage/network references in :app are allowed only in app-owned DI composition modules."
                        } else if (sourceFileName !in appDiCoreImplementationFiles) {
                            violations +=
                                "${sourceFile.relativeTo(projectDir)}:${index + 1}: core data/storage/network references in :app DI are allowed only in core repository or platform provider modules."
                        }
                    }
                }
            }
        }

        productionKotlinSources.files.forEach { sourceFile ->
            val normalizedPath = sourceFile.path.replace('\\', '/')
            if ("/app/src/main/java/com/smarttrainner/app/di/" in normalizedPath) return@forEach

            sourceFile.useLines { lines ->
                lines.forEachIndexed { index, line ->
                    val trimmed = line.trimStart()
                    if (trimmed.startsWith("@Module") || trimmed.startsWith("@InstallIn(")) {
                        violations +=
                            "${sourceFile.relativeTo(projectDir)}:${index + 1}: production Hilt modules must live in app-owned DI composition modules."
                    }
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
        dependencies.add("androidTestImplementation", libs.androidx.test.runner)

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
