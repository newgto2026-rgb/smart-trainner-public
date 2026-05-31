plugins {
    alias(libs.plugins.kotlin.jvm)
}

base {
    archivesName.set("feature-exercise-domain")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}

kotlin {
    jvmToolchain(17)
}
