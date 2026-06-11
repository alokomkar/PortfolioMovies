plugins {
    id("portfolio.android.library")
    id("portfolio.android.compose")
    id("portfolio.android.hilt")
}

android {
    namespace = "com.sortedqueue.portfolio.core.designsystem"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
