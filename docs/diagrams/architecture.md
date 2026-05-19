# Architecture Diagram

This diagram shows how the app modules relate to each other at a high level.

```mermaid
flowchart TD
    App[":app<br/>Application + APK packaging"] --> AppUi[":app-ui<br/>MainActivity + tab shell"]

    App --> MoviesImpl[":feature:movies:impl"]
    App --> TvImpl[":feature:tv:impl"]
    App --> FavoritesImpl[":feature:favorites:impl"]

    AppUi --> DesignSystem[":core:designsystem<br/>FeatureScreenFactory + shared Compose UI"]
    AppUi --> MoviesApi[":feature:movies:api"]
    AppUi --> TvApi[":feature:tv:api"]
    AppUi --> FavoritesApi[":feature:favorites:api"]
    AppUi --> Model[":core:model<br/>MediaSummary + MediaDetail"]

    MoviesImpl --> MoviesApi
    MoviesImpl --> DesignSystem
    MoviesImpl --> Model
    MoviesImpl --> Network[":core:network<br/>Retrofit TMDB API"]
    MoviesImpl --> Database[":core:database<br/>Room + favorites repository"]

    TvImpl --> TvApi
    TvImpl --> DesignSystem
    TvImpl --> Model
    TvImpl --> Network
    TvImpl --> Database

    FavoritesImpl --> FavoritesApi
    FavoritesImpl --> DesignSystem
    FavoritesImpl --> Model
    FavoritesImpl --> Database

    Database --> Model
    Network --> TMDB["TMDB API"]
    Database --> Room["Room database"]

    Testing[":core:testing<br/>Fakes + coroutine rule"] --> Database
    Testing --> Network
    Testing --> Model
```

## Notes

- `:app-ui` depends on feature API boundaries, not feature implementations.
- `:app` depends on implementation modules so Hilt can discover their bindings at the application graph level.
- `FeatureScreenFactory` allows feature screens to be contributed into the app shell with Hilt multibindings.
- Shared domain-style UI models live in `:core:model` to avoid coupling feature implementations to each other.
