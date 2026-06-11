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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sortedqueue.portfolio.player.VideoItem
import com.sortedqueue.portfolio.player.VideoPlayerScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList


data class TvUiState(
    val isLoading: Boolean = true,
    val shows: List<MediaSummary> = emptyList(),
    val errorMessage: String? = null
)

data class TvDetailUiState(
    val isLoading: Boolean = true,
    val detail: MediaDetail? = null,
    val errorMessage: String? = null,
    val videos: List<com.sortedqueue.portfolio.core.network.TmdbVideoDto> = emptyList(),
    val isResolving: Boolean = false,
    val resolvedPlaylist: List<VideoItem> = emptyList()
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
    private val favoritesRepository: FavoritesRepository,
    private val youtubeStreamResolver: com.sortedqueue.portfolio.core.network.YoutubeStreamResolver
) : ViewModel() {
    private val _uiState = MutableStateFlow(TvDetailUiState())
    val uiState: StateFlow<TvDetailUiState> = _uiState

    private var loadedTvId: Int? = null

    fun loadShow(tvId: Int) {
        if (loadedTvId == tvId) return
        loadedTvId = tvId

        viewModelScope.launch {
            _uiState.value = TvDetailUiState(isLoading = true)
            val detailsResult = runCatching { tmdbApi.tvShowDetails(tvId).toMediaDetail() }
            val videosResult = runCatching { tmdbApi.tvShowVideos(tvId).results }
            
            if (detailsResult.isSuccess) {
                val detail = detailsResult.getOrThrow()
                val videos = videosResult.getOrDefault(emptyList())
                _uiState.value = TvDetailUiState(
                    isLoading = false,
                    detail = detail,
                    videos = videos
                )
                favoritesRepository.observeIsFavorite(tvId, MediaType.Tv).collect { isFavorite ->
                    _uiState.update { state ->
                        state.copy(detail = state.detail?.copy(isFavorite = isFavorite))
                    }
                }
            } else {
                val error = detailsResult.exceptionOrNull()
                _uiState.value = TvDetailUiState(
                    isLoading = false,
                    errorMessage = error?.message ?: "Unable to load TV details"
                )
            }
        }
    }

    fun resolveTrailers(onResolved: (List<VideoItem>) -> Unit) {
        val videos = _uiState.value.videos.filter { it.site == "YouTube" && it.type == "Trailer" }
        if (videos.isEmpty()) {
            onResolved(listOf(
                VideoItem(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    title = "${_uiState.value.detail?.title ?: "TV Show"} - Fallback Trailer",
                    subtitle = "No trailers found on TMDB"
                )
            ))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResolving = true) }
            val resolved = mutableListOf<VideoItem>()
            for (video in videos) {
                val title = video.name ?: "Official Trailer"
                val subtitle = "Source: ${video.site ?: "YouTube"} (${video.size ?: 720}p)"
                val resolvedUrl = youtubeStreamResolver.resolveStreamUrl(video.key)
                if (resolvedUrl != null) {
                    resolved.add(VideoItem(url = resolvedUrl, title = title, subtitle = subtitle))
                } else {
                    resolved.add(VideoItem(
                        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        title = "$title (Fallback Stream)",
                        subtitle = subtitle
                    ))
                }
            }
            _uiState.update { it.copy(isResolving = false, resolvedPlaylist = resolved) }
            onResolved(resolved)
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
            media = state.shows.toImmutableList(),
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
    var activePlaylist by remember { mutableStateOf<ImmutableList<VideoItem>?>(null) }

    when {
        state.isLoading || state.isResolving -> LoadingState()
        state.errorMessage != null -> ErrorState(message = state.errorMessage, onRetry = { viewModel.loadShow(tvId) })
        activePlaylist != null -> VideoPlayerScreen(
            playlist = activePlaylist!!,
            onBack = { activePlaylist = null }
        )
        state.detail != null -> MediaDetailContent(
            detail = state.detail,
            onBack = onBack,
            onFavoriteClick = viewModel::toggleFavorite,
            onPlayTrailers = {
                viewModel.resolveTrailers { playlist ->
                    activePlaylist = playlist.toImmutableList()
                }
            }
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
