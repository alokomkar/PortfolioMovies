package com.sortedqueue.portfolio.favorites.impl

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.sortedqueue.portfolio.core.database.FavoritesRepository
import com.sortedqueue.portfolio.core.designsystem.EmptyState
import com.sortedqueue.portfolio.core.designsystem.FeatureScreenFactory
import com.sortedqueue.portfolio.core.designsystem.FeatureScreenKey
import com.sortedqueue.portfolio.core.designsystem.FeatureTab
import com.sortedqueue.portfolio.core.designsystem.MediaGrid
import com.sortedqueue.portfolio.core.model.MediaSummary
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {
    val favorites: StateFlow<List<MediaSummary>> = favoritesRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun removeFavorite(media: MediaSummary) {
        viewModelScope.launch {
            favoritesRepository.setFavorite(media, false)
        }
    }
}

@Composable
fun FavoritesScreen(
    onMediaSelected: (MediaSummary) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites = viewModel.favorites.collectAsStateWithLifecycle().value

    if (favorites.isEmpty()) {
        EmptyState(message = "Your favorite movies and TV shows will appear here.")
    } else {
        MediaGrid(
            media = favorites,
            onMediaSelected = onMediaSelected,
            onFavoriteClick = viewModel::removeFavorite
        )
    }
}

class FavoritesScreenFactoryImpl @Inject constructor() : FeatureScreenFactory {
    @Composable
    override fun RenderScreen(onMediaSelected: (MediaSummary) -> Unit) {
        FavoritesScreen(onMediaSelected = onMediaSelected)
    }

    @Composable
    override fun RenderDetail(mediaId: Int, onBack: () -> Unit) {
        EmptyState(message = "Open movie and TV details from the Favorites list.")
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesScreenModule {
    @Binds
    @IntoMap
    @FeatureScreenKey(FeatureTab.Favorites)
    abstract fun bindFavoritesScreenFactory(factory: FavoritesScreenFactoryImpl): FeatureScreenFactory
}
