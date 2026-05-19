# Libraries

Project dependency versions are centralized in `gradle/libs.versions.toml`. Module-level usage is declared in each module's `build.gradle.kts`.

## Android And Kotlin

- Android Gradle Plugin: Android build, packaging, resources, and test tasks.
- Kotlin: primary language for app, domain, and test code.
- Kotlin Compose Compiler plugin: Compose compiler integration.
- KSP: annotation processing for Hilt and Room.

## UI

- Jetpack Compose UI: declarative UI foundation.
- Material 3: Compose Material components and theme primitives.
- AndroidX Activity Compose: Compose integration for Android activities.
- AndroidX Lifecycle Runtime Compose: lifecycle-aware state collection with Compose.
- AndroidX Lifecycle ViewModel Compose: ViewModel integration for composables.
- AndroidX Navigation Compose: navigation graph and back stack support in `:app-ui`.
- AndroidX Hilt Navigation Compose: Hilt ViewModel access from Compose destinations.
- Coil Compose: image loading for TMDB posters and backdrops.

## Dependency Injection

- Hilt Android: dependency injection runtime.
- Hilt Compiler: generated Hilt components, bindings, and factories.

## Networking

- Retrofit: TMDB HTTP API interface implementation.
- Retrofit Gson Converter: JSON response parsing.
- OkHttp: HTTP client used by Retrofit.
- OkHttp Logging Interceptor: debug HTTP request/response logging.

## Persistence

- Room Runtime: local SQLite persistence for favorites.
- Room KTX: coroutine and Flow support for Room.
- Room Compiler: generated DAO/database implementation.

## Testing

- JUnit 4: JVM unit test framework.
- Kotlin Coroutines Test: deterministic coroutine and dispatcher testing.
- AndroidX JUnit: Android instrumentation test runner support.
- Espresso Core: Android UI instrumentation test support.
- Compose UI Test JUnit4: Compose instrumentation tests.
- Compose UI Test Manifest: debug manifest support for Compose UI tests.
- Jacoco: merged JVM unit test coverage reports and verification gate.

## Supporting AndroidX

- AndroidX Core KTX: Kotlin-friendly Android framework extensions.
- AppCompat: compatibility support used by Android modules.
- Material Components: Android Material dependency available to modules that need platform Material APIs.
