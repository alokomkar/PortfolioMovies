# Portfolio Movies

Portfolio Movies is a modular Android app built with Jetpack Compose. It uses the TMDB API to show popular movies and TV shows, lets users open detail screens, and supports favorite/unfavorite with local persistence.

## Features

- Movies, TV, and Favorites tabs
- Popular movies and popular TV shows from TMDB
- Movie and TV detail screens
- Favorite/unfavorite from list and detail screens
- Room-backed favorites that survive app restarts
- MVVM presentation layer with Hilt-injected ViewModels
- Project-wide Jacoco unit test coverage report

## Architecture

The app follows MVVM with a multi-module structure.

```text
UI screen (Compose)
  -> ViewModel
    -> Repository / API
      -> Retrofit TMDB service
      -> Room DAO
```

Key choices:

- `:app` owns the Android application, manifest, and final APK packaging.
- `:app-ui` owns the main `Activity`, tab shell, and feature routing.
- Feature implementation modules own their ViewModels and feature UI.
- Core modules hold shared contracts, models, networking, persistence, design-system UI, and test utilities.
- Feature screens are registered into the shell with Hilt multibindings through `FeatureScreenFactory`.

## Diagrams

Mermaid diagrams are available under `docs/diagrams`:

- [Architecture diagram](docs/diagrams/architecture.md): module relationships and boundaries
- [Code flow diagrams](docs/diagrams/code-flow.md): startup, list loading, detail, and favorites flows
- [Class diagram](docs/diagrams/class-diagram.md): major app-owned classes and relationships
- [Dependency injection flow](docs/diagrams/dependency-injection.md): Hilt graph, bindings, and feature factory map

## Modules

```text
PortfolioMovies/
├── app/                         # Android application module and Hilt app entry point
├── app-ui/                      # MainActivity, tab shell, feature routing
├── core/
│   ├── database/                # Room database, FavoritesDao, FavoritesRepository
│   ├── designsystem/            # Shared Compose UI and feature screen contracts
│   ├── model/                   # Shared domain UI models
│   ├── network/                 # Retrofit TMDB API and network DI
│   └── testing/                 # Test fakes and coroutine test rule
├── feature/
│   ├── favorites/
│   │   ├── api/                 # Favorites feature API boundary
│   │   └── impl/                # Favorites screen and ViewModel
│   ├── movies/
│   │   ├── api/                 # Movies feature API boundary
│   │   └── impl/                # Movies list/detail screens and ViewModels
│   └── tv/
│       ├── api/                 # TV feature API boundary
│       └── impl/                # TV list/detail screens and ViewModels
├── gradle/                      # Gradle wrapper and version catalog
├── build.gradle.kts             # Root plugins and Jacoco report task
├── settings.gradle.kts          # Module registration
└── gradle.properties
```

## Libraries

- Jetpack Compose: declarative UI
- Material 3: Compose UI components
- AndroidX Activity Compose: Compose activity integration
- AndroidX Lifecycle ViewModel Compose: ViewModel integration for Compose
- AndroidX Lifecycle Runtime Compose: lifecycle-aware state collection
- Hilt: dependency injection
- AndroidX Hilt Navigation Compose: Hilt ViewModel access from Compose
- Retrofit: TMDB HTTP API client
- OkHttp: HTTP transport and logging
- Gson converter for Retrofit: JSON parsing
- Room: local favorites persistence
- Coil Compose: image loading for TMDB posters/backdrops
- Kotlin Coroutines Test: ViewModel/coroutine unit tests
- JUnit 4: unit test framework
- Jacoco: JVM unit test coverage reports

## TMDB API Key

Create or update `local.properties` with:

```properties
TMDB_API_KEY=your_tmdb_api_key_here
```

`local.properties` is ignored by Git. The key is read by `:core:network` and exposed to the app through generated `BuildConfig` fields.

## Build And Run

Build the debug APK:

```bash
./gradlew :app:assembleDebug
```

Install on a connected device or emulator:

```bash
./gradlew :app:installDebug
```

Run all checks:

```bash
./gradlew check
```

## Tests

Run all debug unit tests:

```bash
./gradlew testDebugUnitTest
```

Run the merged Jacoco report:

```bash
./gradlew jacocoDebugReport
```

Open the HTML report:

```text
build/reports/jacoco/jacocoDebugReport/html/index.html
```

XML report:

```text
build/reports/jacoco/jacocoDebugReport/jacocoDebugReport.xml
```

## Current Coverage

Latest generated with:

```bash
./gradlew jacocoDebugReport
```

```text
Instruction coverage: 35.15% (2491/7086)
Branch coverage:      10.40% (57/548)
Line coverage:        50.15% (338/674)
Complexity coverage:  33.33% (165/495)
Method coverage:      69.83% (162/232)
Class coverage:       73.33% (44/60)
```

Coverage intentionally excludes common generated Android/Hilt/Room artifacts such as `R`, `BuildConfig`, `*_Factory`, `*_Impl`, `Hilt_*`, and aggregated Hilt dependency classes.

## Test Coverage Scope

Current tests cover:

- Favorites repository mapping and favorite/unfavorite behavior
- Movies list ViewModel success and error states
- Movie detail ViewModel mapping and favorite state
- TV list ViewModel success state and favorite toggling
- TV detail ViewModel mapping and favorite state
- Favorites ViewModel observation and removal flow

Shared test utilities live in `:core:testing`.

## Git Hygiene

Ignored files include:

- `local.properties`
- Gradle build outputs
- Android Studio workspace files under `.idea/`
- `.gradle`, `.kotlin`, `.cxx`, and other generated artifacts

Do not commit the TMDB API key or generated build outputs.
