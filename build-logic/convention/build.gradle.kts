plugins {
    `kotlin-dsl`
}

group = "com.sortedqueue.portfolio.buildlogic"

dependencies {
    compileOnly("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    compileOnly("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin.get()}")
    compileOnly("com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.get()}")
    compileOnly("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.3.5")
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "portfolio.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "portfolio.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "portfolio.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "portfolio.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
    }
}
