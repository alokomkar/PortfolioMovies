package com.sortedqueue.portfolio.core.designsystem

import androidx.compose.runtime.Composable
import com.sortedqueue.portfolio.core.model.MediaSummary
import dagger.MapKey

import androidx.compose.runtime.Stable

enum class FeatureTab {
    Movies,
    Tv,
    Favorites
}

@MapKey
annotation class FeatureScreenKey(val value: FeatureTab)

@Stable
interface FeatureScreenFactory {
    @Composable
    fun RenderScreen(onMediaSelected: (MediaSummary) -> Unit)

    @Composable
    fun RenderDetail(mediaId: Int, onBack: () -> Unit)
}
