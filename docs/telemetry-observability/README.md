# Production Telemetry & Observability

This document details the telemetry, performance monitoring, and feature toggling strategies designed to ensure high runtime quality and operational visibility in **PortfolioMovies**.

---

## 1. Custom Performance Monitoring

To measure actual user experience in production, we track **App Start Time** (Cold/Warm/Hot starts) and **Time to First Render (TTFR)** using Firebase Performance Monitoring or Sentry.

### App Start Metrics definitions
*   **Cold Start**: Starts from process creation. Measured from the very beginning of the `Application.onCreate` lifecycle until the first frame is fully rendered to the screen.
*   **Time to First Render (TTFR)**: The duration from when a user navigates to a screen (e.g., clicking on a movie) until the network request resolves and Compose draws the key content (not just the loader).

### Decoupled Tracking Architecture
We wrap telemetry tools behind clean domain-level interfaces to prevent Firebase/Sentry classes from scattering throughout the codebase.

```kotlin
interface PerformanceTracker {
    fun startTrace(traceName: String)
    fun stopTrace(traceName: String)
    fun incrementMetric(traceName: String, metricName: String, value: Long)
}
```

---

## 2. Jank Tracking (Frame Metrics API)

### Long-Term Vision
We must systematically measure frame drop rates (Jank) to prevent sluggish scroll interactions. Instead of using deprecated drawing-listeners, we leverage Android's native **Frame Metrics API** through `FrameMetricsAggregator`. This tool logs frame rendering times under the hood on a separate thread, avoiding main-thread overhead.

### Implementation Blueprint
Create a jank tracking system managed by the application lifecycle:

```kotlin
import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.core.app.FrameMetricsAggregator

class JankTracker(private val reporter: (jankFrames: Int, totalFrames: Int) -> Unit) : Application.ActivityLifecycleCallbacks {
    
    private val aggregator = FrameMetricsAggregator(FrameMetricsAggregator.DELAY_DURATION)

    override fun onActivityStarted(activity: Activity) {
        aggregator.add(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        val metrics = aggregator.remove(activity)
        if (metrics != null) {
            val durations = metrics[FrameMetricsAggregator.TOTAL_INDEX]
            if (durations != null) {
                var totalFrames = 0
                var jankFrames = 0
                for (i in 0 until durations.size()) {
                    val durationMs = durations.keyAt(i)
                    val count = durations.valueAt(i)
                    totalFrames += count
                    if (durationMs > 16) { // Frames taking > 16.6ms miss the 60fps refresh rate
                        jankFrames += count
                    }
                }
                if (totalFrames > 0) {
                    reporter(jankFrames, totalFrames)
                }
            }
        }
    }

    // Stub remaining lifecycle callbacks...
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
```

---

## 3. Remote Config & Feature Flag Architecture

### Long-Term Vision
We separate feature deployments from app releases. All new features are wrapped behind feature flags. This allows us to perform phased rollouts, run A/B tests, and perform instant rollbacks if a performance regression is detected in production.

### Architecture Structure
We isolate flags inside a dedicated `:core:featureflag` module:

```text
feature-module (Movies)
     │
     ▼
[FeatureFlagProvider] (Interface in :core:featureflag)
     ▲
     │ (Injected via Hilt)
[FirebaseRemoteConfigProvider] OR [LocalConfigProvider]
```

### Decoupled API Definition
```kotlin
interface FeatureFlagProvider {
    fun isFeatureEnabled(flagName: String): Boolean
    fun fetchAndActivate(onComplete: (Boolean) -> Unit)
}
```
If Firebase fails to load, the system falls back seamlessly to the values defined in a local asset JSON configuration.
