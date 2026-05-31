plugins {
    alias(libs.plugins.kotlin.jvm)
}

base {
    archivesName.set("feature-routine-domain")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin {
    jvmToolchain(17)
}
