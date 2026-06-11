# Build Infrastructure & Multi-Module Architecture

This document describes the long-term vision, trade-offs, and implementation blueprints for standardizing Gradle configurations and dependency management across the **PortfolioMovies** multi-module codebase.

---

## 1. Custom Gradle Convention Plugins

### Long-Term Vision
Multi-module projects suffer from build file bloat and duplication. Standardizing compiler options, SDK targets, and common dependencies via Gradle Convention Plugins allows team members to configure a module with a single line of plugin declaration, removing standard Android boilerplate.

### Architecture Choice: `build-logic` (Composite Build) vs. `buildSrc`
We propose using a separate composite build module named `build-logic` rather than the traditional `buildSrc`.

*   **`buildSrc`**: Modifying any script inside `buildSrc` invalidates the build cache for the entire project, forcing a complete clean build.
*   **`build-logic`**: Evaluates separately as a composite build. Changes to build logic only rebuild the affected plugins, ensuring fast local build times.

### Implementation Blueprint

1. Create a `build-logic` directory at the project root.
2. In `build-logic/settings.gradle.kts`, register the plugins and include the version catalog:
   ```kotlin
   dependencyResolutionManagement {
       versionCatalogs {
           create("libs") {
               from(files("../gradle/libs.versions.toml"))
           }
       }
   }
   ```
3. Implement `build-logic/convention/src/main/kotlin/AndroidLibraryConventionPlugin.kt`:
   ```kotlin
   import com.android.build.gradle.LibraryExtension
   import org.gradle.api.Plugin
   import org.gradle.api.Project
   import org.gradle.kotlin.dsl.configure

   class AndroidLibraryConventionPlugin : Plugin<Project> {
       override fun apply(target: Project) {
           with(target) {
               with(pluginManager) {
                   apply("com.android.library")
                   apply("org.jetbrains.kotlin.android")
               }
               extensions.configure<LibraryExtension> {
                   compileSdk = 36
                   defaultConfig {
                       minSdk = 24
                   }
                   compileOptions {
                       sourceCompatibility = JavaVersion.VERSION_11
                       targetCompatibility = JavaVersion.VERSION_11
                   }
               }
           }
       }
   }
   ```
4. Register the plugin in `build-logic/convention/build.gradle.kts`:
   ```kotlin
   gradlePlugin {
       plugins {
           register("androidLibrary") {
               id = "portfolio.android.library"
               implementationClass = "AndroidLibraryConventionPlugin"
           }
       }
   }
   ```
5. Apply the plugin to feature/core modules:
   ```kotlin
   plugins {
       id("portfolio.android.library")
   }
   ```

---

## 2. Version Catalogs with Renovate or Dependabot

### Long-Term Vision
Using `libs.versions.toml` guarantees that all modules resolve dependencies to the same version. To ensure we don't fall behind on security patches and library advancements, we must automate dependency updates.

### Workflow Blueprint (Dependabot)
Add `.github/dependabot.yml` to trigger weekly dependency updates:
```yaml
version: 2
updates:
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 5
    target-branch: "main"
```

### Trade-offs
*   **Renovate**: Offers granular customization (e.g., grouping Kotlin/Compose updates into a single PR).
*   **Dependabot**: Native integration in GitHub requiring zero external configuration. Highly recommended for initial setup.

---

## 3. Dependency Analysis Tooling

### Long-Term Vision
Over time, refactoring modules leaves unused dependencies in Gradle configuration files. Unused compile-time dependencies leak transitives, bloating the compile classpath and slowing down builds.

We use **Autonomous Apps' Dependency Analysis Gradle Plugin** to enforce build graph hygiene.

### Implementation Blueprint
1. Add the plugin to `gradle/libs.versions.toml`:
   ```toml
   [plugins]
   dependency-analysis = { id = "com.autonomousapps.dependency-analysis", version = "2.6.2" }
   ```
2. Apply the plugin in the root `build.gradle.kts`:
   ```kotlin
   plugins {
       alias(libs.plugins.dependency.analysis)
   }
   ```
3. Run the analysis locally:
   ```bash
   ./gradlew buildHealth
   ```
   This generates recommendations for:
   *   Unused dependencies that should be removed.
   *   Transitive dependencies that are used directly and should be declared explicitly.
   *   Dependencies declared as `api` that should be scoped to `implementation`.
