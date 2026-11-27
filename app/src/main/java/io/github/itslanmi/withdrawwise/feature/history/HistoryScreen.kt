package io.github.itslanmi.withdrawwise.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.ui.components.PlaceholderScreen

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.nav_history),
        icon = R.drawable.ic_receipt_long,
        emptyTitle = stringResource(R.string.placeholder_history_title),
        emptyMessage = stringResource(R.string.placeholder_history_message),
        modifier = modifier
    )
}
