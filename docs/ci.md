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

## Run CI Locally

You can run the same checks locally before opening a PR or pushing to `main`.

First, make sure `local.properties` exists at the project root:

```properties
TMDB_ACCESS_TOKEN=your_tmdb_access_token_here
```

Then run the same commands used by GitHub Actions:

```bash
./gradlew testDebugUnitTest
./gradlew jacocoDebugCoverageVerification
./gradlew :app:assembleDebug
```

For a single local command that mirrors the CI job, run:

```bash
./gradlew testDebugUnitTest jacocoDebugCoverageVerification :app:assembleDebug
```

If this command passes locally, the GitHub Actions workflow should pass for the same commit.

To inspect coverage for a single module while iterating, run that module's Jacoco task:

```bash
./gradlew :feature:movies:impl:jacocoDebugModuleReport
```

To generate reports for all modules:

```bash
./gradlew jacocoDebugModuleReports
```

## Local Reports

After running local CI checks, reports are available at:

```text
build/reports/jacoco/jacocoDebugReport/html/index.html
```

Individual module Jacoco reports are available at:

```text
<module>/build/reports/jacoco/jacocoDebugModuleReport/html/index.html
```

Modules with no debug classes after coverage exclusions may skip report generation.

Module unit test reports are available under:

```text
<module>/build/reports/tests/testDebugUnitTest/index.html
```

For example:

```text
feature/movies/impl/build/reports/tests/testDebugUnitTest/index.html
feature/tv/impl/build/reports/tests/testDebugUnitTest/index.html
core/database/build/reports/tests/testDebugUnitTest/index.html
```

## Local Troubleshooting

- If Gradle cannot find the TMDB access token, confirm `local.properties` exists and contains `TMDB_ACCESS_TOKEN`.
- If coverage verification fails, open the Jacoco HTML report and inspect missed instructions or branches.
- If a module test fails, open that module's `testDebugUnitTest` HTML report.
- If dependencies fail to resolve, rerun with a stable network connection because Gradle may need to download artifacts.
- If Android SDK errors appear, open the project in Android Studio once and let it install the required SDK packages.

## Required Secret

Add the TMDB access token as a GitHub Actions repository secret:

```text
Name: TMDB_ACCESS_TOKEN
Value: your_tmdb_access_token
```

GitHub location:

```text
Repository -> Settings -> Secrets and variables -> Actions -> New repository secret
```

The workflow writes this value into `local.properties` during CI:

```properties
TMDB_ACCESS_TOKEN=<secret value>
```

Do not commit `local.properties` or the access token.

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
