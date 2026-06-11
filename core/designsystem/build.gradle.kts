plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

composeCompiler {
    enableStrongSkippingMode = true
    stabilityConfigurationFile = rootProject.file("compose_stability_config.conf")
    metricsDestination = layout.buildDirectory.dir("compose_compiler/metrics")
    reportsDestination = layout.buildDirectory.dir("compose_compiler/reports")
}


android {
    namespace = "com.sortedqueue.portfolio.core.designsystem"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
    implementation(libs.hilt.android)
    implementation(libs.material)

    // Compose stability enforcement & lints
    lintChecks(libs.slack.compose.lints)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
