plugins {
    alias(libs.plugins.kotlin.jvm)
}

base {
    archivesName.set("feature-workout-domain")
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.javax.inject)
}

kotlin {
    jvmToolchain(17)
}
