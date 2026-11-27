package io.github.itslanmi.withdrawwise.feature.insights

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.ui.components.PlaceholderScreen

@Composable
fun InsightsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.nav_insights),
        icon = R.drawable.ic_bar_chart,
        emptyTitle = stringResource(R.string.placeholder_insights_title),
        emptyMessage = stringResource(R.string.placeholder_insights_message),
        modifier = modifier
    )
}
