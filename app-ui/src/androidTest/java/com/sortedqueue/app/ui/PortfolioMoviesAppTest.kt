package com.sortedqueue.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sortedqueue.portfolio.core.designsystem.FeatureScreenFactory
import com.sortedqueue.portfolio.core.designsystem.FeatureTab
import com.sortedqueue.portfolio.core.model.MediaSummary
import com.sortedqueue.portfolio.core.model.MediaType
import org.junit.Rule
import org.junit.Test

class PortfolioMoviesAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun systemBackReturnsFromDetailToSelectedTab() {
        composeRule.setContent {
            PortfolioMoviesApp(
                featureScreens = mapOf(
                    FeatureTab.Movies to moviesScreen(),
                    FeatureTab.Tv to unavailableScreen("TV"),
                    FeatureTab.Favorites to unavailableScreen("Favorites")
                )
            )
        }

        composeRule.onNodeWithText("Open movie").performClick()
        composeRule.onNodeWithText("Movie detail").assertIsDisplayed()

        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithText("Open movie").assertIsDisplayed()
    }
}

private fun moviesScreen(): FeatureScreenFactory {
    return object : FeatureScreenFactory {
        @Composable
        override fun RenderScreen(onMediaSelected: (MediaSummary) -> Unit) {
            Button(
                onClick = {
                    onMediaSelected(
                        MediaSummary(
                            id = 11,
                            type = MediaType.Movie,
                            title = "Star Wars",
                            overview = "A space opera.",
                            posterPath = null,
                            backdropPath = null,
                            releaseDate = "1977-05-25",
                            voteAverage = 8.2
                        )
                    )
                }
            ) {
                Text(text = "Open movie")
            }
        }

        @Composable
        override fun RenderDetail(mediaId: Int, onBack: () -> Unit) {
            Text(text = "Movie detail")
        }
    }
}

private fun unavailableScreen(label: String): FeatureScreenFactory {
    return object : FeatureScreenFactory {
        @Composable
        override fun RenderScreen(onMediaSelected: (MediaSummary) -> Unit) {
            Text(text = label)
        }

        @Composable
        override fun RenderDetail(mediaId: Int, onBack: () -> Unit) {
            Text(text = "$label detail")
        }
    }
}
