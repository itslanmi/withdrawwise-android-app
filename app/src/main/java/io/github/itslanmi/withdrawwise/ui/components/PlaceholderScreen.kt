package io.github.itslanmi.withdrawwise.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Temporary screen body used while a feature is still being built. */
@Composable
fun PlaceholderScreen(
    title: String,
    @DrawableRes icon: Int,
    emptyTitle: String,
    emptyMessage: String,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().statusBarsPadding().padding(bottom = LocalBottomBarPadding.current)) {
        ScreenHeader(title = title)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(icon = icon, title = emptyTitle, message = emptyMessage)
        }
    }
}
