plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.smarttrainner.feature.analysis.entry"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":feature:analysis:api"))
    implementation(project(":feature:analysis:impl"))

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    androidTestImplementation(libs.androidx.test.runner)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

kapt {
    correctErrorTypes = true
}
