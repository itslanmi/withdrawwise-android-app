package io.github.itslanmi.withdrawwise.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.itslanmi.withdrawwise.ui.theme.extended

val FloatingNavBarHeight = 64.dp

/**
 * Pill-shaped navigation bar that floats above the content. The selected tab expands into a
 * brand-coloured capsule with a filled icon and its label.
 */
@Composable
fun FloatingNavBar(
    selected: TopLevelDestination?,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = MaterialTheme.extended
    Surface(
        shape = CircleShape,
        color = extended.navBar,
        contentColor = extended.onNavBar,
        shadowElevation = 12.dp,
        border = if (extended.isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
        modifier = modifier.height(FloatingNavBarHeight)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp).selectableGroup()
        ) {
            TopLevelDestination.entries.forEach { destination ->
                NavItem(destination = destination, selected = destination == selected, onClick = {
                    onSelect(destination)
                })
            }
        }
    }
}

@Composable
private fun NavItem(destination: TopLevelDestination, selected: Boolean, onClick: () -> Unit) {
    val extended = MaterialTheme.extended
    val background by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "nav-bg"
    )
    val content by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else extended.onNavBarMuted,
        label = "nav-fg"
    )
    val label = stringResource(destination.label)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .height(48.dp)
                .clip(CircleShape)
                .background(background)
                .selectable(selected = selected, onClick = onClick, role = Role.Tab)
                .padding(horizontal = 14.dp)
    ) {
        Icon(
            painter = painterResource(if (selected) destination.selectedIcon else destination.icon),
            contentDescription = label,
            tint = content,
            modifier = Modifier.size(24.dp)
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + expandHorizontally(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Row {
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
            }
        }
    }
}
