# CI Pipeline

The project uses GitHub Actions for continuous integration. The workflow lives in:

```text
.github/workflows/android-ci.yml
```

## When It Runs

CI runs for:

- Pull requests targeting `main`
- Pushes to `main`

Only one run per branch/ref stays active at a time. New pushes cancel older in-progress runs for the same ref.

## What It Checks

The pipeline runs these commands:

```bash
./gradlew testDebugUnitTest
./gradlew jacocoDebugCoverageVerification
./gradlew :app:assembleDebug
```

That means a PR must pass:

- Debug JVM unit tests
- Merged Jacoco coverage verification
- Debug APK build

The coverage verification task currently enforces the 90% instruction coverage gate configured in the root `build.gradle.kts`.

## Required Secret

Add the TMDB API key as a GitHub Actions repository secret:

```text
Name: TMDB_API_KEY
Value: your_tmdb_api_key
```

GitHub location:

```text
Repository -> Settings -> Secrets and variables -> Actions -> New repository secret
```

The workflow writes this value into `local.properties` during CI:

```properties
TMDB_API_KEY=<secret value>
```

Do not commit `local.properties` or the API key.

## Reports

The workflow uploads reports as artifacts, even when a check fails:

- `jacoco-debug-report`: merged Jacoco HTML report
- `unit-test-reports`: module unit test HTML reports

In GitHub Actions, open the workflow run and check the Artifacts section to download them.

## Updating CI

Keep CI changes in the same repository as the app so build and test requirements evolve with the code.

When adding new checks:

- Prefer Gradle tasks that contributors can also run locally.
- Keep secrets in GitHub Actions secrets, not in workflow files.
- Update this document when commands, required secrets, or artifacts change.
