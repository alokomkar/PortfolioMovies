plugins {
    id("portfolio.android.library")
    id("portfolio.android.compose")
    id("portfolio.android.hilt")
}

android {
    namespace = "com.sortedqueue.portfolio.favorites.impl"
}

dependencies {
    // Core dependencies this implementation will need
    implementation(project(":core:database"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    testImplementation(project(":core:testing"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
