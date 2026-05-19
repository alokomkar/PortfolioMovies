# Production Readiness

This project is a portfolio app, but it is structured to show how the same codebase could move toward production quality.

## Current Strengths

- Modular MVVM architecture with clear app, feature, and core ownership.
- Hilt dependency injection with feature registration through multibindings.
- Retrofit and OkHttp network layer with bearer-token authentication.
- Room-backed favorites persistence.
- Unit tests for repositories, mappers, ViewModels, and network auth behavior.
- Jacoco coverage verification with a 90% instruction coverage gate.
- GitHub Actions CI for tests, coverage, and debug APK assembly.
- Architecture diagrams, testing guidance, library inventory, and ADRs.

## Runtime States

The app should continue to treat these states as first-class product behavior:

- Loading state while fetching movie or TV data.
- Error state with a retry path when network calls fail.
- Empty state for favorites and future empty API responses.
- Image fallback when TMDB poster or backdrop paths are missing.
- Detail state that preserves favorite/unfavorite behavior.
- System back behavior from detail screens to the selected tab.

## Security And Secrets

`TMDB_ACCESS_TOKEN` is read from `local.properties` locally and from GitHub Actions secrets in CI. The token is sent as a bearer token by the network layer.

For a production app, do not treat this as a complete secret-management solution. Mobile binaries can be inspected, so sensitive API credentials should be protected by a backend service, short-lived tokens, or a server-side proxy depending on the product requirements.

## Offline And Caching

Favorites are local-first and persist through Room. Popular Movies and TV lists currently come from the network. A stronger production strategy would cache list responses in Room, show stale cached data immediately, and refresh in the background when connectivity is available.

## Quality Gates

Local and CI checks should stay aligned:

```bash
./gradlew testDebugUnitTest jacocoDebugCoverageVerification :app:assembleDebug
```

Before merging larger changes, contributors should also consider:

- Running connected Compose tests for shell-level UI behavior.
- Reviewing Jacoco missed branches for meaningful business logic gaps.
- Updating ADRs when architecture decisions change.
- Updating diagrams when module dependencies or runtime flows change.

## Known Limitations

- Popular Movies and TV lists are not paginated yet.
- Network list responses are not cached for offline browsing.
- There is no release signing or Play Store distribution setup.
- There are no architecture boundary tests yet.
- UI tests cover the shell back behavior, but broader Compose screen coverage can be expanded.
- No static analysis tool such as Detekt or ktlint is currently configured.

## Recommended Next Investments

- Add Paging 3 for popular Movies and TV lists.
- Add Room-backed cache for list responses with a refresh policy.
- Add dependency boundary checks for module rules.
- Add Detekt or ktlint to CI.
- Add database migration tests before schema changes.
- Add more connected UI tests for empty, error, and favorite flows.

