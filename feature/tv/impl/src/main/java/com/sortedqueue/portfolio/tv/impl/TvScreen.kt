package com.sortedqueue.portfolio.tv.impl

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

data class TvUiState(
    val isLoading: Boolean = true,
    val shows: List<MediaSummary> = emptyList(),
    val errorMessage: String? = null
)

data class TvDetailUiState(
    val isLoading: Boolean = true,
    val detail: MediaDetail? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TvViewModel @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TvUiState())
    val uiState: StateFlow<TvUiState> = _uiState

    init {
        observeFavorites()
        loadShows()
    }

    fun loadShows() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { tmdbApi.popularTvShows().results.map { it.toMediaSummary() } }
                .onSuccess { shows ->
                    _uiState.update { it.copy(isLoading = false, shows = shows) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Unable to load TV shows") }
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
                val favoriteTvIds = favorites.filter { it.type == MediaType.Tv }.map { it.id }.toSet()
                _uiState.update { state ->
                    state.copy(shows = state.shows.map { it.copy(isFavorite = it.id in favoriteTvIds) })
                }
            }
        }
    }
}

@HiltViewModel
class TvDetailViewModel @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TvDetailUiState())
    val uiState: StateFlow<TvDetailUiState> = _uiState

    private var loadedTvId: Int? = null

    fun loadShow(tvId: Int) {
        if (loadedTvId == tvId) return
        loadedTvId = tvId

        viewModelScope.launch {
            _uiState.value = TvDetailUiState(isLoading = true)
            runCatching { tmdbApi.tvShowDetails(tvId).toMediaDetail() }
                .onSuccess { detail ->
                    _uiState.value = TvDetailUiState(isLoading = false, detail = detail)
                    favoritesRepository.observeIsFavorite(tvId, MediaType.Tv).collect { isFavorite ->
                        _uiState.update { state ->
                            state.copy(detail = state.detail?.copy(isFavorite = isFavorite))
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.value = TvDetailUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load TV details"
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
fun TvScreen(
    onMediaSelected: (MediaSummary) -> Unit,
    viewModel: TvViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    when {
        state.isLoading -> LoadingState()
        state.errorMessage != null -> ErrorState(message = state.errorMessage, onRetry = viewModel::loadShows)
        state.shows.isEmpty() -> EmptyState(message = "No TV shows found.")
        else -> MediaGrid(
            media = state.shows,
            onMediaSelected = onMediaSelected,
            onFavoriteClick = viewModel::toggleFavorite
        )
    }
}

@Composable
fun TvDetailScreen(
    tvId: Int,
    onBack: () -> Unit,
    viewModel: TvDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(tvId) {
        viewModel.loadShow(tvId)
    }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    when {
        state.isLoading -> LoadingState()
        state.errorMessage != null -> ErrorState(message = state.errorMessage, onRetry = { viewModel.loadShow(tvId) })
        state.detail != null -> MediaDetailContent(
            detail = state.detail,
            onBack = onBack,
            onFavoriteClick = viewModel::toggleFavorite
        )
    }
}

class TvScreenFactoryImpl @Inject constructor() : FeatureScreenFactory {
    @Composable
    override fun RenderScreen(onMediaSelected: (MediaSummary) -> Unit) {
        TvScreen(onMediaSelected = onMediaSelected)
    }

    @Composable
    override fun RenderDetail(mediaId: Int, onBack: () -> Unit) {
        TvDetailScreen(tvId = mediaId, onBack = onBack)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TvScreenModule {
    @Binds
    @IntoMap
    @FeatureScreenKey(FeatureTab.Tv)
    abstract fun bindTvScreenFactory(factory: TvScreenFactoryImpl): FeatureScreenFactory
}
