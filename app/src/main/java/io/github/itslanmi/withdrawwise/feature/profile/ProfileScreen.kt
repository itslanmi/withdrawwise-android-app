package io.github.itslanmi.withdrawwise.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.ui.components.PlaceholderScreen

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.nav_profile),
        icon = R.drawable.ic_person,
        emptyTitle = stringResource(R.string.placeholder_profile_title),
        emptyMessage = stringResource(R.string.placeholder_profile_message),
        modifier = modifier
    )
}
