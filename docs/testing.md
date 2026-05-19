# Testing Guide

This project uses JVM unit tests for production logic in repositories, mappers, and ViewModels. Compose rendering code is intentionally kept out of the Jacoco gate because those paths are better covered with UI or screenshot tests.

## Commands

Run all debug unit tests:

```bash
./gradlew testDebugUnitTest
```

Generate the merged Jacoco report:

```bash
./gradlew jacocoDebugReport
```

Generate Jacoco reports for every module:

```bash
./gradlew jacocoDebugModuleReports
```

Generate a Jacoco report for one module:

```bash
./gradlew :feature:movies:impl:jacocoDebugModuleReport
```

Generate the report and enforce the 90% instruction coverage gate:

```bash
./gradlew jacocoDebugCoverageVerification
```

Open the HTML report at:

```text
build/reports/jacoco/jacocoDebugReport/html/index.html
```

Module-specific HTML reports are generated at:

```text
<module>/build/reports/jacoco/jacocoDebugModuleReport/html/index.html
```

For example:

```text
feature/movies/impl/build/reports/jacoco/jacocoDebugModuleReport/html/index.html
feature/tv/impl/build/reports/jacoco/jacocoDebugModuleReport/html/index.html
core/database/build/reports/jacoco/jacocoDebugModuleReport/html/index.html
```

Modules with no debug classes after coverage exclusions may skip report generation.

## Where Tests Live

Place tests next to the module they validate:

```text
core/database/src/test/...          # Repository tests
feature/movies/impl/src/test/...    # Movies ViewModel and mapper tests
feature/tv/impl/src/test/...        # TV ViewModel and mapper tests
feature/favorites/impl/src/test/... # Favorites ViewModel tests
```

Shared fakes and coroutine rules live in:

```text
core/testing/src/main/java/com/sortedqueue/portfolio/core/testing
```

## ViewModel Test Pattern

Use `MainDispatcherRule` for ViewModels because they launch work on `viewModelScope`.

```kotlin
class ExampleViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun viewModel_exposesLoadedState() = runTest {
        val api = FakeTmdbApi().apply {
            popularMoviesResult = Result.success(
                TmdbPagedResponse(
                    page = 1,
                    results = listOf(movieDto(id = 10, title = "Heat")),
                    total_pages = 1,
                    total_results = 1
                )
            )
        }
        val repository = FavoritesRepository(FakeFavoritesDao())
        val viewModel = MoviesViewModel(api, repository)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Heat", viewModel.uiState.value.movies.single().title)
    }
}
```

## What To Cover

Prefer tests that prove behavior rather than implementation details:

- Success state from the API or repository
- Error state with the thrown message
- Fallback error state when the throwable has no message
- Empty state where applicable
- Favorite and unfavorite transitions
- Filtering logic, especially media type checks
- Duplicate-load guards for detail ViewModels
- DTO-to-domain mapper fallbacks for null or blank TMDB fields
- Singular/plural labels such as TV season text

## Mapper Tests

Mappers are plain Kotlin functions and should be tested without ViewModels. Keep mapper functions in non-Compose files so Jacoco reports them as normal production logic.

```kotlin
@Test
fun movieMapper_usesFallbackTextForBlankFields() {
    val summary = TmdbMovieDto(
        id = 1,
        title = "",
        overview = null,
        poster_path = null,
        backdrop_path = null,
        release_date = null,
        vote_average = null
    ).toMediaSummary()

    assertEquals("Untitled movie", summary.title)
    assertEquals("No overview available.", summary.overview)
}
```

## Coroutine Notes

The test dispatcher is unconfined by default, so most `viewModelScope.launch` work completes immediately. If a test starts collecting a `StateFlow` or relies on delayed subscription behavior, use `backgroundScope.launch { ... }` and `advanceUntilIdle()`.

Cancel collector jobs when they are no longer needed:

```kotlin
val collectionJob = backgroundScope.launch {
    viewModel.favorites.collect {}
}

advanceUntilIdle()

collectionJob.cancel()
```

## Coverage Scope

The merged and module-specific Jacoco reports exclude generated Android/Hilt/Room artifacts, Compose rendering functions, screen factories, theme wrappers, and shared test fakes. The coverage gate is meant to track app-owned production logic:

- ViewModels
- Repositories
- DTO-to-domain mappers
- Domain model behavior
- Network DTO construction

If new production logic lands in a module, add a focused unit test in the same module and rerun:

```bash
./gradlew jacocoDebugCoverageVerification
```

If you only need feedback for the module you changed, run that module's report first:

```bash
./gradlew :feature:movies:impl:jacocoDebugModuleReport
```
