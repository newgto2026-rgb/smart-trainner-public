plugins {
    alias(libs.plugins.kotlin.jvm)
}

base {
    archivesName.set("feature-analysis-domain")
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}

kotlin {
    jvmToolchain(17)
}
