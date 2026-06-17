plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

val serverBaseUrlProperty = providers.gradleProperty("smarttrainner.serverBaseUrl")

fun quotedBuildConfigString(value: String) = "\"$value\""

android {
    namespace = "com.smarttrainner.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "SMART_TRAINNER_SERVER_BASE_URL",
                quotedBuildConfigString(
                    serverBaseUrlProperty
                        .orElse("https://specifies-cup-cross-latina.trycloudflare.com/")
                        .get()
                )
            )
        }
        release {
            buildConfigField(
                "String",
                "SMART_TRAINNER_SERVER_BASE_URL",
                quotedBuildConfigString(serverBaseUrlProperty.orElse("").get())
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)

    testImplementation(libs.junit)
    testImplementation(libs.truth)

    androidTestImplementation(libs.androidx.test.runner)
}

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst {
        check(serverBaseUrlProperty.isPresent) {
            "Release builds require -Psmarttrainner.serverBaseUrl=https://<server-host>/"
        }
    }
}
