# Jetpack Compose Platform Architecture

This document outlines the performance guardrails, styling restrictions, custom design system rules, and compiler stability configurations established to keep Compose UI fast, consistent, and maintainable across the **PortfolioMovies** codebase.

---

## 1. How to Check for Recompositions (Runtime Audits)

### The Android Studio Layout Inspector

To check for active recompositions visually at runtime:

1. Connect your physical device or emulator and run the app in a debuggable state.
2. Navigate to **View > Tool Windows > Layout Inspector** in Android Studio.
3. Once attached, verify your UI hierarchy is rendering.
4. Toggle on **Show Recomposition Counts** in the inspector settings panel.
5. Two values will appear adjacent to each composable node:
   *   **Recomposition Count**: Shows how many times this specific node has re-run.
   *   **Skipped Count**: Shows how many times this node bypassed execution safely because its input parameters were stable and unchanged.

> [!TIP]
> If a composable node's recomposition count increases continuously during user interactions (like list scrolling or text inputs) but its direct inputs have not changed, it means the compiler has classified one or more parameters as unstable.

---

## 2. Compile-time Stability Checks (Compose Compiler Metrics)

While the Layout Inspector monitors runtime behavior, to diagnose *why* a class is unstable, we use Compose Compiler Metrics.

### Step 1: Gradle Configuration
We configure the `composeCompiler` block in each module's build script:

```kotlin
composeCompiler {
    // Generate static reports on class stability and composable skippability
    metricsDestination = layout.buildDirectory.dir("compose_compiler/metrics")
    reportsDestination = layout.buildDirectory.dir("compose_compiler/reports")
}
```

### Step 2: Run Compile Tasks
Execute the compiler output tasks on a release build for correct stability inference:
```bash
./gradlew assembleRelease
```

### Step 3: Analyze Reports
Navigate to your module's `build/compose_compiler/` folder. Two major reports are generated:

#### 1. Composables Report (`*_composables.txt`)
Details the skippability status of every function:
```text
// Example of a healthy, optimized composable
restartable skippable fun MoviePosterCard(
    stable media: MediaSummary,
    stable onClick: () -> Unit
)
```
*   **restartable**: The function acts as a boundary that can be re-run if its inputs change.
*   **skippable**: The function can be bypassed completely if its inputs are unchanged. **This is our target state.**

#### 2. Classes Report (`*_classes.txt`)
Details why data structures are deemed unstable by the compiler:
```text
// Example of an unstable collection input
unstable class MovieDetailUiState {
    stable val isLoading: Boolean
    unstable val genres: List<String> // Unstable because java.util.List could be mutable!
}
```

---

## 3. Enforcing Stability Rules (CI/CD Guardrails)

We automate stability enforcement in our local checks and build pipelines.

### Strategy A: Slack Compose Lints
We integrate **Brose/Slack's `compose-lints`** library into our build dependencies. This catches bad patterns in static analysis before the code compile step.

1. Add the lint library to the project dependencies:
   ```kotlin
   dependencies {
       lintChecks(libs.slack.compose.lints)
   }
   ```
2. Configure `lint.xml` at the root project directory to escalate critical stability issues to build-blocking errors:
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <lint>
       <issue id="ComposeUnstableCollections" severity="error" />
       <issue id="ComposeUnstableReceiver" severity="error" />
       <issue id="ComposeMutableParameters" severity="error" />
   </lint>
   ```

### Strategy B: Stability Configuration Files
Instead of cluttering domain models and DTO files with redundant `@Stable` or `@Immutable` annotations, we define a global configuration file to mark entire packages or external models as implicitly stable.

1. Create a `compose_stability_config.conf` file at your project root:
   ```text
   # Treat all models in our shared model module as stable implicitly
   com.sortedqueue.portfolio.core.model.**
   
   # Treat standard library datetimes as stable
   kotlinx.datetime.LocalDateTime
   ```
2. Reference the configuration file inside your `composeCompiler` Gradle block:
   ```kotlin
   composeCompiler {
       stabilityConfigurationFile = rootProject.file("compose_stability_config.conf")
   }
   ```

### Strategy C: Strong Skipping Mode
We force strong skipping mode inside our Compose Compiler configurations:
```kotlin
composeCompiler {
    enableStrongSkippingMode = true
}
```
*Strong Skipping Mode* allows the compiler to skip recomposition of composables even when they have unstable parameters, as long as their values are structurally equal (`equals()` comparisons).

### Strategy D: Kotlinx Immutable Collections
The most common cause of class instability is standard Java Collections interfaces (`List`, `Set`, `Map`) because the compiler cannot guarantee they won't mutate at runtime.

To solve this, we enforce that all UI state models use **Kotlinx Immutable Collections**:
```kotlin
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val genres: ImmutableList<String> = persistentListOf() // Stable!
)
```
Combined with the Slack Lint rules, this blocks PRs trying to use standard collections in composable parameters.

---

## 4. Custom Design System Theming

### Spacing Tokens
We declare design system spacing tokens via a custom data class and Local provider:

```kotlin
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val default: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
```

Wrap the main design system theme with the composition provider:

```kotlin
@Composable
fun PortfolioMoviesTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSpacing provides Spacing()
    ) {
        MaterialTheme(
            colorScheme = AppColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

// Global accessor object for developers
object AppTheme {
    val spacing: Spacing
        @Composable
        get() = LocalSpacing.current
}
```

### Usage in Feature Screens
```kotlin
@Composable
fun MovieListItem(movie: Movie, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(AppTheme.spacing.small) // Safe, tokenized spacing
    ) {
        // Content
    }
}
```
This guarantees consistent spacing and colors across features without XML or Compose style overrides.
