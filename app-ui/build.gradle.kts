plugins {
    id("portfolio.android.library")
    id("portfolio.android.compose")
    id("portfolio.android.hilt")
}

android {
    namespace = "com.sortedqueue.portfolio.app.ui"
}

dependencies {
    // 1. Core Architectural / Design Modules
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
