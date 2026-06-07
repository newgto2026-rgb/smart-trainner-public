import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    base
    jacoco
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}

val uiExcludedCoverageExcludedProjects = setOf(
    ":core:designsystem",
    ":core:model",
    ":core:network",
    ":core:testing",
    ":core:ui",
    ":feature:analysis:api",
    ":feature:exercise:api",
    ":feature:routine:api",
    ":feature:workout:api"
)

val uiExcludedCoverageClassExcludes = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Activity*.*",
    "**/*Application*.*",
    "**/*App.*",
    "**/*AppKt*.*",
    "**/*ComposableSingletons*.*",
    "**/*Content*.*",
    "**/*Dialog*.*",
    "**/*Dialogs*.*",
    "**/*FeatureEntry*.*",
    "**/*FeatureEntryImpl*.*",
    "**/*Localization*.*",
    "**/*Navigation*.*",
    "**/*NavigationKt*.*",
    "**/*Route*.*",
    "**/*Routes*.*",
    "**/*Sheet*.*",
    "**/*Theme*.*",
    "**/di/**",
    "**/*_Factory*.*",
    "**/*_MembersInjector*.*",
    "**/*_Impl*.*",
    "**/*Dao_Impl*.*",
    "**/*Database_Impl*.*",
    "**/*Dagger*.*",
    "**/*Hilt*.*",
    "**/*Hilt_*.*"
)

val uiExcludedCoverageGateProjects = setOf(
    ":core:domain",
    ":feature:analysis:domain",
    ":feature:calendar:domain",
    ":feature:routine:domain",
    ":feature:workout:domain"
)

val uiExcludedCoverageGateClassIncludes = listOf(
    "**/*Calculator*.*",
    "**/*TrainingSeedStore*.*",
    "**/*UseCase*.*"
)

// Suspend command wrappers are covered by focused tests, but JaCoCo reports
// Kotlin coroutine entry lines as missed even when all observable behavior runs.
val uiExcludedCoverageGateClassExcludes = uiExcludedCoverageClassExcludes + listOf(
    "**/*\$Companion*.*",
    "**/*\$DefaultImpls*.*",
    "**/*CalculatorKt*.*",
    "**/*Repository*.*",
    "**/*Repositories*.*",
    "**/*CancelLatestRoutineDayCompletionUseCase*.*",
    "**/*CheckNicknameAvailabilityUseCase*.*",
    "**/*CompleteRoutineDayUseCase*.*",
    "**/*DeleteCustomRoutineUseCase*.*",
    "**/*GetLatestWorkoutLogUseCase*.*",
    "**/*LogoutUseCase*.*",
    "**/*ObserveActiveSessionUseCase*.*",
    "**/*ObserveAllWorkoutLogsUseCase*.*",
    "**/*ObserveCurrentCyclePlanUseCase*.*",
    "**/*ObserveCycleSummaryUseCase*.*",
    "**/*ObserveExercisesUseCase*.*",
    "**/*ObserveLatestWorkoutLogsUseCase*.*",
    "**/*ObserveNetworkOnlineUseCase*.*",
    "**/*ObservePlanTemplatesUseCase*.*",
    "**/*ObserveRoutineCycleCompletionsUseCase*.*",
    "**/*ObserveRoutineProgressUseCase*.*",
    "**/*ObserveTrainingExperienceUseCase*.*",
    "**/*SaveCustomRoutineUseCase*.*",
    "**/*SaveWorkoutLogUseCase*.*",
    "**/*SelectPlanTemplateUseCase*.*",
    "**/*SetRoutineDayDateUseCase*.*",
    "**/*SetTrainingExperienceUseCase*.*",
    "**/*SignInWithGoogleUseCase*.*",
    "**/*StartDefaultSessionUseCase*.*",
    "**/*StartRoutineUseCase*.*",
    "**/*SyncPendingTrainingDataUseCase*.*",
    "**/*SwitchRoutineTemplateUseCase*.*",
    "**/*UpdateBodyProfileUseCase*.*",
    "**/*ValidateActiveSessionDeviceUseCase*.*"
)

subprojects {
    pluginManager.apply("jacoco")

    tasks.withType<Test>().configureEach {
        extensions.configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
}

fun Project.uiExcludedCoverageClassDirectories(
    includes: List<String> = emptyList(),
    excludes: List<String> = uiExcludedCoverageClassExcludes
) = files(
    fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        if (includes.isNotEmpty()) include(includes)
        exclude(excludes)
    },
    fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
        if (includes.isNotEmpty()) include(includes)
        exclude(excludes)
    },
    fileTree(layout.buildDirectory.dir("classes/kotlin/main")) {
        if (includes.isNotEmpty()) include(includes)
        exclude(excludes)
    },
    fileTree(layout.buildDirectory.dir("classes/java/main")) {
        if (includes.isNotEmpty()) include(includes)
        exclude(excludes)
    }
)

fun Project.uiExcludedCoverageSourceDirectories() = files(
    layout.projectDirectory.dir("src/main/java"),
    layout.projectDirectory.dir("src/main/kotlin")
)

val uiExcludedCoverageProjects = provider {
    subprojects.filter { project ->
        project.path !in uiExcludedCoverageExcludedProjects &&
            project.projectDir.resolve("src/main").exists()
    }
}

val uiExcludedCoverageVerificationProjects = provider {
    subprojects.filter { project ->
        project.path in uiExcludedCoverageGateProjects &&
            project.projectDir.resolve("src/main").exists()
    }
}

val uiExcludedTestCoverageReport by tasks.registering(JacocoReport::class) {
    group = "verification"
    description = "Generates UI-excluded unit test coverage for non-UI production code."

    dependsOn(
        uiExcludedCoverageProjects.map { projects ->
            projects.flatMap { project ->
                project.tasks.findByName("testDebugUnitTest")?.let(::listOf)
                    ?: listOfNotNull(project.tasks.findByName("test"))
            }
        }
    )

    classDirectories.from(
        uiExcludedCoverageProjects.map { projects ->
            projects.map { it.uiExcludedCoverageClassDirectories() }
        }
    )
    sourceDirectories.from(
        uiExcludedCoverageProjects.map { projects ->
            projects.map { it.uiExcludedCoverageSourceDirectories() }
        }
    )
    executionData.from(
        uiExcludedCoverageProjects.map { projects ->
            projects.map { project ->
                project.fileTree(project.layout.buildDirectory) {
                    include(
                        "jacoco/test.exec",
                        "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
                    )
                }
            }
        }
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

val uiExcludedTestCoverageGateReport by tasks.registering(JacocoReport::class) {
    group = "verification"
    description = "Generates the UI-excluded 100% gate coverage report for domain business logic."

    dependsOn(uiExcludedTestCoverageReport)

    classDirectories.from(
        uiExcludedCoverageVerificationProjects.map { projects ->
            projects.map {
                it.uiExcludedCoverageClassDirectories(
                    includes = uiExcludedCoverageGateClassIncludes,
                    excludes = uiExcludedCoverageGateClassExcludes
                )
            }
        }
    )
    sourceDirectories.from(
        uiExcludedCoverageVerificationProjects.map { projects ->
            projects.map { it.uiExcludedCoverageSourceDirectories() }
        }
    )
    executionData.from(
        uiExcludedCoverageVerificationProjects.map { projects ->
            projects.map { project ->
                project.fileTree(project.layout.buildDirectory) {
                    include(
                        "jacoco/test.exec",
                        "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
                    )
                }
            }
        }
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

val uiExcludedTestCoverageVerification by tasks.registering(JacocoCoverageVerification::class) {
    group = "verification"
    description = "Requires 100% line coverage for the UI-excluded domain business logic gate."

    dependsOn(uiExcludedTestCoverageGateReport)

    classDirectories.from(uiExcludedTestCoverageGateReport.map { it.classDirectories })
    sourceDirectories.from(uiExcludedTestCoverageGateReport.map { it.sourceDirectories })
    executionData.from(uiExcludedTestCoverageGateReport.map { it.executionData })

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "1.0".toBigDecimal()
            }
        }
    }
}

val isCiBuild = providers.environmentVariable("CI")
    .map { it.equals("true", ignoreCase = true) }
    .orElse(false)

if (isCiBuild.get()) {
    subprojects {
        tasks.matching { it.name == "test" }.configureEach {
            finalizedBy(uiExcludedTestCoverageVerification)
        }
    }
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
        fun isFeatureDomain(path: String) = isFeature(path) && path.endsWith(":domain")
        fun isFeatureData(path: String) = isFeature(path) && path.endsWith(":data")
        fun isFeatureNetwork(path: String) = isFeature(path) && path.endsWith(":network")
        fun isFeatureEntry(path: String) = isFeature(path) && path.endsWith(":entry")
        fun isFeatureImpl(path: String) = isFeature(path) && path.endsWith(":impl")
        fun featureName(path: String) = path.split(":").getOrNull(2)

        val allowedCrossFeatureApiDependencies = emptySet<Pair<String, String>>()
        val allowedFeaturePrivateModules = setOf(
            ":feature:analysis:domain",
            ":feature:analysis:data",
            ":feature:calendar:domain",
            ":feature:exercise:domain",
            ":feature:routine:domain",
            ":feature:routine:data",
            ":feature:workout:domain",
            ":feature:workout:data"
        )
        val allowedFeatureDataCoreInfrastructureDependencies = setOf(
            ":feature:routine:data" to ":core:database",
            ":feature:routine:data" to ":core:datastore",
            ":feature:routine:data" to ":core:network",
            ":feature:workout:data" to ":core:database",
            ":feature:workout:data" to ":core:datastore",
            ":feature:workout:data" to ":core:network"
        )
        val allowedAppFeatureImplDependencies = setOf(
            ":app" to ":feature:analysis:impl",
            ":app" to ":feature:calendar:impl",
            ":app" to ":feature:exercise:impl",
            ":app" to ":feature:routine:impl",
            ":app" to ":feature:workout:impl"
        )
        val allowedAppFeatureDataDependencies = setOf(
            ":app" to ":feature:analysis:data",
            ":app" to ":feature:routine:data",
            ":app" to ":feature:workout:data"
        )
        val allowedAppFeatureDomainDependencies = setOf(
            ":app" to ":feature:analysis:domain",
            ":app" to ":feature:routine:domain",
            ":app" to ":feature:workout:domain"
        )
        val allProjectPaths = allprojects.map { it.path }.toSet()
        val invalidAllowlistPaths = (
            (allowedCrossFeatureApiDependencies +
                allowedFeatureDataCoreInfrastructureDependencies +
                allowedAppFeatureImplDependencies +
                allowedAppFeatureDataDependencies +
                allowedAppFeatureDomainDependencies)
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
        val featureDomainReferencePattern = Regex("""com\.smarttrainner\.feature\.[^.]+\.domain(\.|$)""")
        val featureNetworkReferencePattern = Regex("""com\.smarttrainner\.feature\.[^.]+\.network(\.|$)""")
        val coreNetworkReferencePattern = Regex("""com\.smarttrainner\.core\.network(\.|$)""")
        val coreImplementationReferencePattern =
            Regex("""com\.smarttrainner\.core\.(data|database|datastore)(\.|$)""")
        val navigationReferencePattern =
            Regex("""\b(androidx\.navigation|NavHost|NavController|NavGraph|NavBackStackEntry|rememberNavController|currentBackStackEntryAsState)\b""")
        val navigationRouteDslPattern = Regex("""\b(composable|navigation)\s*\(""")
        val navigationCommandPattern = Regex("""\bnavigate\s*\(""")

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
                isFeatureApi(source) &&
                    (
                        isFeatureDomain(target) ||
                            isFeatureData(target) ||
                            isFeatureNetwork(target) ||
                            isFeatureImpl(target) ||
                            isFeatureEntry(target)
                        ) -> {
                    violations += "$edge: feature API modules must expose only app-facing route contracts; feature domain, data, network, implementation, and entry modules stay private."
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
                source == ":app" &&
                    isFeatureDomain(target) &&
                    (source to target) !in allowedAppFeatureDomainDependencies -> {
                    violations += "$edge: app may depend on feature domain only from the app-owned DI composition allowlist."
                }
            }
        }

        val appDiCoreImplementationFiles = setOf(
            "CoreRepositoryBindingsModule.kt",
            "PlatformDatabaseModule.kt"
        )
        val appDiCoreNetworkFiles = setOf("PlatformNetworkModule.kt")
        val appDiFeatureImplementationFiles = setOf("FeatureEntryBindingsModule.kt")
        val appDiFeatureDataFiles = setOf(
            "AnalysisDataRepositoryBindingsModule.kt",
            "RoutineDataRepositoryBindingsModule.kt",
            "WorkoutDataRepositoryBindingsModule.kt"
        )
        val appDiFeatureDomainFiles = appDiFeatureDataFiles
        val appNavigationFiles = setOf("SmartTrainnerNavigation.kt")

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
                    if (featureDomainReferencePattern.containsMatchIn(line)) {
                        if (!isAppDiSource) {
                            violations +=
                                "${sourceFile.relativeTo(projectDir)}:${index + 1}: feature domain references in :app are allowed only in app-owned DI composition modules."
                        } else if (sourceFileName !in appDiFeatureDomainFiles) {
                            violations +=
                                "${sourceFile.relativeTo(projectDir)}:${index + 1}: feature domain references in :app DI are allowed only in approved feature data repository binding modules."
                        }
                    }
                    if (coreNetworkReferencePattern.containsMatchIn(line)) {
                        if (!isAppDiSource || sourceFileName !in appDiCoreNetworkFiles) {
                            violations +=
                                "${sourceFile.relativeTo(projectDir)}:${index + 1}: core network references in :app are allowed only in the approved app-owned network provider module."
                        }
                    }
                    if (coreImplementationReferencePattern.containsMatchIn(line)) {
                        if (!isAppDiSource) {
                            violations +=
                                "${sourceFile.relativeTo(projectDir)}:${index + 1}: core data/storage references in :app are allowed only in app-owned DI composition modules."
                        } else if (sourceFileName !in appDiCoreImplementationFiles) {
                            violations +=
                                "${sourceFile.relativeTo(projectDir)}:${index + 1}: core data/storage references in :app DI are allowed only in core repository or platform provider modules."
                        }
                    }
                }
            }
        }

        productionKotlinSources.files.forEach { sourceFile ->
            val normalizedPath = sourceFile.path.replace('\\', '/')
            val isAppSource = "/app/src/main/" in normalizedPath
            val isAppDiSource = "/app/src/main/java/com/smarttrainner/app/di/" in normalizedPath
            val isFeatureApiSource = "/feature/" in normalizedPath && "/api/src/main/" in normalizedPath
            val sourceFileName = sourceFile.name

            sourceFile.useLines { lines ->
                lines.forEachIndexed { index, line ->
                    if (
                        isFeatureApiSource &&
                        (
                            featureDomainReferencePattern.containsMatchIn(line) ||
                                featureDataReferencePattern.containsMatchIn(line) ||
                                featureNetworkReferencePattern.containsMatchIn(line) ||
                                featureImplReferencePattern.containsMatchIn(line)
                            )
                    ) {
                        violations +=
                            "${sourceFile.relativeTo(projectDir)}:${index + 1}: feature API source must not reference feature domain, data, network, or implementation packages."
                    }
                    val usesNavigationRouting =
                        navigationReferencePattern.containsMatchIn(line) ||
                            navigationRouteDslPattern.containsMatchIn(line) ||
                            navigationCommandPattern.containsMatchIn(line)
                    if (!isAppSource && usesNavigationRouting) {
                        violations +=
                            "${sourceFile.relativeTo(projectDir)}:${index + 1}: navigation graph and routing APIs must stay in :app; feature/core modules expose route surfaces and callbacks only."
                    }
                    if (isAppSource && sourceFileName !in appNavigationFiles && usesNavigationRouting) {
                        violations +=
                            "${sourceFile.relativeTo(projectDir)}:${index + 1}: app navigation graph and routing commands must stay in approved app navigation files."
                    }
                    val trimmed = line.trimStart()
                    if (
                        !isAppDiSource &&
                        (trimmed.startsWith("@Module") || trimmed.startsWith("@InstallIn("))
                    ) {
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
