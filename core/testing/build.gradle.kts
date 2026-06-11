plugins {
    id("portfolio.android.library")
}

android {
    namespace = "com.sortedqueue.portfolio.core.testing"
}

dependencies {
    api(project(":core:database"))
    api(project(":core:model"))
    api(project(":core:network"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.kotlinx.coroutines.test)
    implementation(libs.material)
    api(libs.junit)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
