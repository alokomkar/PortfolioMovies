package com.sortedqueue.portfolio.movies.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.sortedqueue.portfolio.core.database.FavoritesRepository
import com.sortedqueue.portfolio.core.designsystem.EmptyState
import com.sortedqueue.portfolio.core.designsystem.ErrorState
import com.sortedqueue.portfolio.core.designsystem.FeatureScreenFactory
import com.sortedqueue.portfolio.core.designsystem.FeatureScreenKey
import com.sortedqueue.portfolio.core.designsystem.FeatureTab
import com.sortedqueue.portfolio.core.designsystem.LoadingState
import com.sortedqueue.portfolio.core.designsystem.MediaDetailContent
import com.sortedqueue.portfolio.core.designsystem.MediaGrid
import com.sortedqueue.portfolio.core.model.MediaDetail
import com.sortedqueue.portfolio.core.model.MediaSummary
import com.sortedqueue.portfolio.core.model.MediaType
import com.sortedqueue.portfolio.core.network.TmdbApi
import com.sortedqueue.portfolio.core.network.TmdbMovieDetailDto
import com.sortedqueue.portfolio.core.network.TmdbMovieDto
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoviesUiState(
    val isLoading: Boolean = true,
    val movies: List<MediaSummary> = emptyList(),
    val errorMessage: String? = null
)

data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val detail: MediaDetail? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState

    init {
        observeFavorites()
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { tmdbApi.popularMovies().results.map { it.toMediaSummary() } }
                .onSuccess { movies ->
                    _uiState.update { it.copy(isLoading = false, movies = movies) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Unable to load movies") }
                }
        }
    }

    fun toggleFavorite(media: MediaSummary) {
        viewModelScope.launch {
            favoritesRepository.setFavorite(media, !media.isFavorite)
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.observeFavorites().collect { favorites ->
                val favoriteMovieIds = favorites.filter { it.type == MediaType.Movie }.map { it.id }.toSet()
                _uiState.update { state ->
                    state.copy(movies = state.movies.map { it.copy(isFavorite = it.id in favoriteMovieIds) })
                }
            }
        }
    }
}

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState

    private var loadedMovieId: Int? = null

    fun loadMovie(movieId: Int) {
        if (loadedMovieId == movieId) return
        loadedMovieId = movieId

        viewModelScope.launch {
            _uiState.value = MovieDetailUiState(isLoading = true)
            runCatching { tmdbApi.movieDetails(movieId).toMediaDetail() }
                .onSuccess { detail ->
                    _uiState.value = MovieDetailUiState(isLoading = false, detail = detail)
                    favoritesRepository.observeIsFavorite(movieId, MediaType.Movie).collect { isFavorite ->
                        _uiState.update { state ->
                            state.copy(detail = state.detail?.copy(isFavorite = isFavorite))
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.value = MovieDetailUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load movie details"
                    )
                }
        }
    }

    fun toggleFavorite() {
        val detail = _uiState.value.detail ?: return
        viewModelScope.launch {
            favoritesRepository.setFavorite(detail, !detail.isFavorite)
        }
    }
}

@Composable
fun MoviesScreen(
    onMediaSelected: (MediaSummary) -> Unit,
    viewModel: MoviesViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    when {
        state.isLoading -> LoadingState()
        state.errorMessage != null -> ErrorState(message = state.errorMessage, onRetry = viewModel::loadMovies)
        state.movies.isEmpty() -> EmptyState(message = "No movies found.")
        else -> MediaGrid(
            media = state.movies,
            onMediaSelected = onMediaSelected,
            onFavoriteClick = viewModel::toggleFavorite
        )
    }
}

@Composable
fun MovieDetailScreen(
    movieId: Int,
    onBack: () -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(movieId) {
        viewModel.loadMovie(movieId)
    }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    when {
        state.isLoading -> LoadingState()
        state.errorMessage != null -> ErrorState(message = state.errorMessage, onRetry = { viewModel.loadMovie(movieId) })
        state.detail != null -> MediaDetailContent(
            detail = state.detail,
            onBack = onBack,
            onFavoriteClick = viewModel::toggleFavorite
        )
    }
}

class MovieScreenFactoryImpl @Inject constructor() : FeatureScreenFactory {
    @Composable
    override fun RenderScreen(onMediaSelected: (MediaSummary) -> Unit) {
        MoviesScreen(onMediaSelected = onMediaSelected)
    }

    @Composable
    override fun RenderDetail(mediaId: Int, onBack: () -> Unit) {
        MovieDetailScreen(movieId = mediaId, onBack = onBack)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MovieScreenModule {
    @Binds
    @IntoMap
    @FeatureScreenKey(FeatureTab.Movies)
    abstract fun bindMovieScreenFactory(factory: MovieScreenFactoryImpl): FeatureScreenFactory
}

private fun TmdbMovieDto.toMediaSummary(): MediaSummary {
    return MediaSummary(
        id = id,
        type = MediaType.Movie,
        title = title.orEmpty().ifBlank { "Untitled movie" },
        overview = overview.orEmpty().ifBlank { "No overview available." },
        posterPath = poster_path,
        backdropPath = backdrop_path,
        releaseDate = release_date,
        voteAverage = vote_average
    )
}

private fun TmdbMovieDetailDto.toMediaDetail(): MediaDetail {
    return MediaDetail(
        id = id,
        type = MediaType.Movie,
        title = title.orEmpty().ifBlank { "Untitled movie" },
        overview = overview.orEmpty().ifBlank { "No overview available." },
        posterPath = poster_path,
        backdropPath = backdrop_path,
        releaseDate = release_date,
        voteAverage = vote_average,
        runtimeLabel = runtime?.let { "$it min" },
        genres = genres.orEmpty().map { it.name }
    )
}
