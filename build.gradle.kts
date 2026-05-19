// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // 1. Core Android Build Environment Tools (Must be declared first)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    // 2. Base Kotlin and Compose Tools
    alias(libs.plugins.kotlin.compose) apply false

    // 3. Code Generation Processors (Must come after Android/Kotlin base packages)
    id("com.google.devtools.ksp") version "2.3.5" apply false
    alias(libs.plugins.hilt.android) apply false
}
