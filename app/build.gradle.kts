plugins {
    id("portfolio.android.application")
    id("portfolio.android.compose")
    id("portfolio.android.hilt")
}

android {
    namespace = "com.sortedqueue.portfolio"
    defaultConfig {
        applicationId = "com.sortedqueue.portfolio"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    // 1. Pull in the main UI orchestration shell
    implementation(project(":app-ui"))

    // 2. Pull in all concrete feature implementations (Satisfies Hilt dependency requirements)
    implementation(project(":feature:movies:impl"))
    implementation(project(":feature:tv:impl"))
    implementation(project(":feature:favorites:impl"))

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(project(":core:network"))
    debugImplementation(libs.flipper)
    debugImplementation(libs.soloader)
}
