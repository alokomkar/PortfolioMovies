import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

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
    jacoco
}

jacoco {
    toolVersion = "0.8.13"
}

subprojects {
    apply(plugin = "jacoco")

    tasks.withType<Test>().configureEach {
        extensions.configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
}

tasks.register<JacocoReport>("jacocoDebugReport") {
    group = "verification"
    description = "Runs debug unit tests and generates a merged Jacoco coverage report."

    val reportProjects = subprojects.filter { it.buildFile.exists() }
    val testTaskPaths = reportProjects.map { "${it.path}:testDebugUnitTest" }
    dependsOn(testTaskPaths)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoDebugReport/jacocoDebugReport.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jacocoDebugReport/html"))
    }

    val coverageExclusions = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Application*.*",
        "**/*Activity*.*",
        "**/*Module*.*",
        "**/*_Factory*.*",
        "**/*_MembersInjector*.*",
        "**/*_Impl*.*",
        "**/Hilt_*.*",
        "**/hilt_aggregated_deps/**",
        "**/dagger/hilt/**"
    )

    classDirectories.setFrom(
        files(
            reportProjects.flatMap { project ->
                listOf(
                    fileTree(project.layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
                        exclude(coverageExclusions)
                    },
                    fileTree(project.layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")) {
                        exclude(coverageExclusions)
                    },
                    fileTree(project.layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                        exclude(coverageExclusions)
                    }
                )
            }
        )
    )
    sourceDirectories.setFrom(
        files(
            reportProjects.flatMap { project ->
                listOf(
                    "${project.projectDir}/src/main/java",
                    "${project.projectDir}/src/main/kotlin"
                )
            }
        )
    )
    executionData.setFrom(
        fileTree(rootDir) {
            include(
                "**/build/jacoco/testDebugUnitTest.exec",
                "**/build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
            )
        }
    )
}
