package io.github.itslanmi.withdrawwise.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.ui.components.PlaceholderScreen

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.nav_map),
        icon = R.drawable.ic_map_filled,
        emptyTitle = stringResource(R.string.placeholder_map_title),
        emptyMessage = stringResource(R.string.placeholder_map_message),
        modifier = modifier
    )
}
