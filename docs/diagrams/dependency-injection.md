# Dependency Injection Flow

The app uses Hilt for dependency injection. The application module depends on feature implementation modules so Hilt can aggregate feature bindings into the app graph.

```mermaid
flowchart TD
    App["@HiltAndroidApp<br/>PortfolioMoviesApplication"] --> Singleton["SingletonComponent"]

    Singleton --> NetworkModule["NetworkModule<br/>@InstallIn(SingletonComponent)"]
    NetworkModule --> OkHttp["OkHttpClient"]
    NetworkModule --> Retrofit["Retrofit"]
    NetworkModule --> TmdbApi["TmdbApi"]
    OkHttp --> Retrofit
    Retrofit --> TmdbApi

    Singleton --> DatabaseModule["DatabaseModule<br/>@InstallIn(SingletonComponent)"]
    DatabaseModule --> RoomDb["PortfolioMoviesDatabase"]
    DatabaseModule --> FavoritesDao["FavoritesDao"]
    RoomDb --> FavoritesDao

    Singleton --> FavoritesRepository["FavoritesRepository @Inject"]
    FavoritesDao --> FavoritesRepository

    Singleton --> MovieScreenModule["MovieScreenModule<br/>@Binds @IntoMap"]
    Singleton --> TvScreenModule["TvScreenModule<br/>@Binds @IntoMap"]
    Singleton --> FavoritesScreenModule["FavoritesScreenModule<br/>@Binds @IntoMap"]

    MovieScreenModule --> MovieFactory["FeatureTab.Movies -> MovieScreenFactoryImpl"]
    TvScreenModule --> TvFactory["FeatureTab.Tv -> TvScreenFactoryImpl"]
    FavoritesScreenModule --> FavoritesFactory["FeatureTab.Favorites -> FavoritesScreenFactoryImpl"]

    MovieFactory --> FactoryMap["Map<FeatureTab, FeatureScreenFactory>"]
    TvFactory --> FactoryMap
    FavoritesFactory --> FactoryMap

    Singleton --> Activity["@AndroidEntryPoint<br/>MainActivity"]
    FactoryMap --> Activity

    Activity --> Compose["PortfolioMoviesApp"]
    Compose --> ScreenFactory["Selected FeatureScreenFactory"]

    TmdbApi --> MoviesVm["MoviesViewModel @HiltViewModel"]
    FavoritesRepository --> MoviesVm
    TmdbApi --> MovieDetailVm["MovieDetailViewModel @HiltViewModel"]
    FavoritesRepository --> MovieDetailVm

    TmdbApi --> TvVm["TvViewModel @HiltViewModel"]
    FavoritesRepository --> TvVm
    TmdbApi --> TvDetailVm["TvDetailViewModel @HiltViewModel"]
    FavoritesRepository --> TvDetailVm

    FavoritesRepository --> FavoritesVm["FavoritesViewModel @HiltViewModel"]
```

## Binding Summary

| Binding | Provider | Scope |
| --- | --- | --- |
| `OkHttpClient` | `NetworkModule.provideOkHttpClient()` | `@Singleton` |
| `Retrofit` | `NetworkModule.provideRetrofit()` | `@Singleton` |
| `TmdbApi` | `NetworkModule.provideTmdbApi()` | `@Singleton` |
| `PortfolioMoviesDatabase` | `DatabaseModule.provideDatabase()` | `@Singleton` |
| `FavoritesDao` | `DatabaseModule.provideFavoritesDao()` | Unscoped provider |
| `FavoritesRepository` | `@Inject constructor` | `@Singleton` |
| `FeatureScreenFactory` map entries | Feature `@Binds @IntoMap` modules | `SingletonComponent` |
| Feature ViewModels | `@HiltViewModel @Inject constructor` | ViewModel scope |

## Why The App Module Depends On Feature Implementations

`:app-ui` only needs the feature API contracts and shared `FeatureScreenFactory` type. The final `:app` module depends on each feature implementation so Hilt can discover and aggregate:

- `MovieScreenModule`
- `TvScreenModule`
- `FavoritesScreenModule`
- all `@HiltViewModel` classes in feature modules

This keeps the shell decoupled while still allowing the final application graph to include all feature bindings.
