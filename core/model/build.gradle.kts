plugins {
    id("portfolio.android.library")
}

android {
    namespace = "com.sortedqueue.portfolio.core.model"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
}
