plugins {
    id("portfolio.android.library")
    id("portfolio.android.compose")
}

android {
    namespace = "com.sortedqueue.portfolio.player"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Media3 dependencies
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)

    // YouTube Player dependency
    implementation(libs.android.youtube.player)
}
