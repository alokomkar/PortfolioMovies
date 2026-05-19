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

## Contributor Docs

- [CI Pipeline](docs/ci.md): GitHub Actions checks, required secrets, and uploaded reports
- [Libraries](docs/libraries.md): dependency list, purpose of each library, and version management
- [Testing Guide](docs/testing.md): test structure, examples, and coverage scope

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

Run the same build, test, and coverage checks used by CI:

```bash
./gradlew testDebugUnitTest jacocoDebugCoverageVerification :app:assembleDebug
```

See [CI Pipeline](docs/ci.md) for local CI instructions, reports, and troubleshooting.

## Tests

For collaborator guidance and examples, see the [Testing Guide](docs/testing.md).

Run all debug unit tests:

```bash
./gradlew testDebugUnitTest
```

Run the merged Jacoco report:

```bash
./gradlew jacocoDebugReport
```

Run the merged Jacoco report and enforce the 90% instruction coverage gate:

```bash
./gradlew jacocoDebugCoverageVerification
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
./gradlew jacocoDebugCoverageVerification
```

```text
Instruction coverage: 92.19% (2527/2741)
Branch coverage:      79.03% (98/124)
Line coverage:        96.99% (322/332)
```

Coverage intentionally excludes generated Android/Hilt/Room artifacts, Compose rendering functions, screen factories, theme wrappers, and shared test fakes so the gate focuses on production business logic, mappers, repositories, and ViewModels.

## Test Coverage Scope

Current tests cover:

- Favorites repository mapping and favorite/unfavorite behavior
- Movies list ViewModel success and error states
- Movie DTO-to-domain mapping, detail state, error state, and favorite state
- Movie favorite and unfavorite branches, duplicate detail-load guard, and fallback error messages
- TV list ViewModel success and error states with favorite toggling
- TV DTO-to-domain mapping, detail state, error state, and favorite state
- TV favorite and unfavorite branches, duplicate detail-load guard, and fallback error messages
- Favorites ViewModel observation and removal flow

Shared test utilities live in `:core:testing`.

## Git Hygiene

Ignored files include:

- `local.properties`
- Gradle build outputs
- Android Studio workspace files under `.idea/`
- `.gradle`, `.kotlin`, `.cxx`, and other generated artifacts

Do not commit the TMDB API key or generated build outputs.
