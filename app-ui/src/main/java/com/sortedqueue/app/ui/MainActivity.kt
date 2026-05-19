package com.sortedqueue.app.ui

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sortedqueue.app.ui.theme.PortfolioMoviesTheme
import com.sortedqueue.portfolio.core.designsystem.FeatureScreenFactory
import com.sortedqueue.portfolio.core.designsystem.FeatureTab
import com.sortedqueue.portfolio.core.model.MediaSummary
import com.sortedqueue.portfolio.core.model.MediaType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var featureScreens: Map<FeatureTab, @JvmSuppressWildcards FeatureScreenFactory>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PortfolioMoviesTheme {
                PortfolioMoviesApp(featureScreens = featureScreens)
            }
        }
    }
}

@Composable
fun PortfolioMoviesApp(
    featureScreens: Map<FeatureTab, @JvmSuppressWildcards FeatureScreenFactory>,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(FeatureTab.Movies, FeatureTab.Tv, FeatureTab.Favorites)
    var selectedTab by rememberSaveable { mutableStateOf(FeatureTab.Movies) }
    var selectedMedia by remember { mutableStateOf<MediaSummary?>(null) }
    val detailTab = selectedMedia?.type?.toFeatureTab()

    BackHandler(enabled = selectedMedia != null) {
        selectedMedia = null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (selectedMedia == null) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {},
                            label = { Text(text = tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            val media = selectedMedia
            if (media != null && detailTab != null) {
                featureScreens[detailTab]?.RenderDetail(
                    mediaId = media.id,
                    onBack = { selectedMedia = null }
                ) ?: Text(text = "Feature unavailable: ${detailTab.label}")
            } else {
                featureScreens[selectedTab]?.RenderScreen(
                    onMediaSelected = { mediaSummary ->
                        selectedMedia = mediaSummary
                    }
                ) ?: Text(text = "Feature unavailable: ${selectedTab.label}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PortfolioMoviesAppPreview() {
    PortfolioMoviesTheme {
        PortfolioMoviesApp(
            featureScreens = mapOf(
                FeatureTab.Movies to previewScreen("Movies"),
                FeatureTab.Tv to previewScreen("TV"),
                FeatureTab.Favorites to previewScreen("Favorites")
            )
        )
    }
}

private val FeatureTab.label: String
    get() = when (this) {
        FeatureTab.Movies -> "Movies"
        FeatureTab.Tv -> "TV"
        FeatureTab.Favorites -> "Favorites"
    }

private fun MediaType.toFeatureTab(): FeatureTab {
    return when (this) {
        MediaType.Movie -> FeatureTab.Movies
        MediaType.Tv -> FeatureTab.Tv
    }
}

private fun previewScreen(label: String): FeatureScreenFactory {
    return object : FeatureScreenFactory {
        @Composable
        override fun RenderScreen(onMediaSelected: (MediaSummary) -> Unit) {
            Text(text = label, modifier = Modifier.padding(24.dp))
        }

        @Composable
        override fun RenderDetail(mediaId: Int, onBack: () -> Unit) {
            Text(text = "$label detail", modifier = Modifier.padding(24.dp))
        }
    }
}
