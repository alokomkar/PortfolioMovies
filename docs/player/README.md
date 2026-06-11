# Reusable ExoPlayer Module (`:player`)

This document outlines the architectural patterns, component breakdowns, and code structures of the reusable media player library module `:player`.

---

## 1. Architectural Patterns & Decoupled Design

To ensure the media player can be dropped into any other project without carrying domain dependencies of this specific application (like TMDb API structures, database configurations, or navigation utilities), it is designed as a standalone, self-contained Android Library module.

### Core Domain Model
The player operates exclusively on a generic data class, representing playable assets:

```kotlin
data class VideoItem(
    val url: String,
    val title: String,
    val subtitle: String? = null
)
```
Any feature that wishes to trigger playback must map its feature-specific model (such as `MediaDetail`) into `List<VideoItem>` before launching the player screen.

---

## 2. Component Design & State Management

The module utilizes custom state hoisting to separate video engine operations (ExoPlayer APIs) from Jetpack Compose UI rendering.

```text
  [Feature Screen] (Movies/TV Show Detail Screen)
         │
         ▼ (Passes List<VideoItem>)
  [VideoPlayerScreen]
         │
         ├───► [rememberVideoPlayerState] (Manages ExoPlayer instantiation, progress polling & release)
         │
         ├───► [VideoPlayerView] (Wraps Media3 PlayerView + displays overlay controls)
         │
         └───► [LazyColumn (Playlist)] (Interactive playlist sidebar queue)
```

### VideoPlayerState & Lifecycle Hooks
The state class `VideoPlayerState` coordinates ExoPlayer listeners and tracks key playback metadata (isPlaying, current item index, duration, currentPosition) via Compose State values.

*   **Continuous Position Polling**: Because ExoPlayer doesn't natively expose a flow of milliseconds elapsed, we run a coroutine loop when `state.isPlaying` is true to update progress indicators:
    ```kotlin
    LaunchedEffect(state.isPlaying) {
        if (state.isPlaying) {
            while (true) {
                state.currentPosition = exoPlayer.currentPosition
                state.duration = exoPlayer.duration
                delay(250) // Polls 4 times a second
            }
        }
    }
    ```
*   **Lifecycle Disposal**: ExoPlayer resource leaks can crash the application or waste memory. We guarantee correct cleanup using a `DisposableEffect`:
    ```kotlin
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }
    ```

---

## 3. Playback Custom Controls

Instead of relying on the default platform control panel overlay, this module renders custom overlay controls using Material3 widgets:

1.  **Play/Pause**: Toggles the ExoPlayer play/pause flags.
2.  **Stop**: Halts playback completely and seeks back to the start of the current item.
3.  **Rewind (-10s)**: Seeks backwards by 10,000ms, capped at `0L`.
4.  **Forward (+10s)**: Seeks forward by 10,000ms, capped at total duration.
5.  **Next**: Triggers `player.seekToNextMediaItem()` (enabled only if not at the end of the queue).
6.  **Previous**: Triggers `player.seekToPreviousMediaItem()` (enabled only if index > 0).

---

## 4. Playlist Queueing

*   **Auto-Advancement**: The playlist queue is populated inside ExoPlayer using `player.setMediaItems(list)`. When an item finishes playing, ExoPlayer automatically transitions to the next item in the queue.
*   **Queue Navigation**: Clicking on any item inside the interactive playlist sidebar/list triggers `state.playAtIndex(index)`, which calls `player.seekTo(index, 0L)` and resumes playback.
*   **Current Item Highlight**: The active index state allows the composable list to style the currently playing item differently (e.g. using a distinct background tint and red borders) for a premium look.

---

## 5. How to Reuse This Module in Other Projects

Because the `:player` module is built to be domain-agnostic and fully self-contained, copying it into a new project is simple:

### Step 1: Copy the Module Files
Copy the physical `player` directory from the root of this project into the root directory of your target Android project:
```bash
cp -r /path/to/PortfolioMovies/player /path/to/YourTargetProject/
```

### Step 2: Register the Module
Add the `:player` module to your target project's `settings.gradle.kts` file:
```kotlin
include(":player")
```

### Step 3: Define Media3 Dependencies
Ensure your target project has version mappings for Androidx Media3 in its `libs.versions.toml` file under the `[versions]` and `[libraries]` sections:

```toml
[versions]
media3 = "1.3.1"

[libraries]
androidx-media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
androidx-media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
androidx-media3-common = { group = "androidx.media3", name = "media3-common", version.ref = "media3" }
```

### Step 4: Import and Call in Compose UI
1. Add the module as a dependency in the `build.gradle.kts` of the feature module where you want to show the video player:
   ```kotlin
   dependencies {
       implementation(project(":player"))
   }
   ```
2. Map your project's custom data model into a list of generic `VideoItem` models, and render the `VideoPlayerScreen` Composable:
   ```kotlin
   import com.sortedqueue.portfolio.player.VideoItem
   import com.sortedqueue.portfolio.player.VideoPlayerScreen

   @Composable
   fun MyScreen() {
       var showPlayer by remember { mutableStateOf(false) }

       if (showPlayer) {
           val playlist = listOf(
               VideoItem(
                   url = "https://example.com/video1.mp4",
                   title = "Trailer 1",
                   subtitle = "HD Stream"
               ),
               VideoItem(
                   url = "https://example.com/video2.mp4",
                   title = "Trailer 2",
                   subtitle = "SD Stream"
               )
           )
           
           VideoPlayerScreen(
               playlist = playlist,
               onBack = { showPlayer = false }
           )
       }
   }
   ```

