package io.github.itslanmi.withdrawwise.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Row of pill tabs. The selected pill is high-contrast, the rest are soft grey. */
@Composable
fun SegmentedPills(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val container by animateColorAsState(
                if (selected) colors.inverseSurface else colors.surfaceContainer,
                label = "pill-container"
            )
            val content by animateColorAsState(
                if (selected) colors.inverseOnSurface else colors.onSurfaceVariant,
                label = "pill-content"
            )
            Surface(
                onClick = { onSelect(index) },
                shape = CircleShape,
                color = container,
                contentColor = content,
                modifier =
                    Modifier.semantics {
                        role = Role.Tab
                        this.selected = selected
                    }
            ) {
                Box(Modifier.padding(horizontal = 18.dp, vertical = 9.dp)) {
                    Text(label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
