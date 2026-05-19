plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.sortedqueue.portfolio.tv.impl"
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

}

dependencies {
    // Link the tv API contract
    implementation(project(":feature:tv:api"))

    // Core dependencies this implementation will need
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    testImplementation(project(":core:testing"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}