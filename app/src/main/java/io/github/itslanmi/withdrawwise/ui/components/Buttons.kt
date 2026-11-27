package io.github.itslanmi.withdrawwise.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.ui.theme.extended

enum class PillStyle {
    /** High-contrast pill: black in light mode, white in dark mode. The main call to action. */
    Primary,

    /** Brand-coloured pill, for moments that should feel celebratory. */
    Accent,

    /** Soft grey pill for secondary actions. */
    Tonal,

    /** Outlined pill for tertiary actions. */
    Outline
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillStyle = PillStyle.Primary,
    @DrawableRes leadingIcon: Int? = null,
    trailingChevron: Boolean = false,
    enabled: Boolean = true,
    compact: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    val (container, content) =
        when (style) {
            PillStyle.Primary -> colors.inverseSurface to colors.inverseOnSurface
            PillStyle.Accent -> colors.primary to colors.onPrimary
            PillStyle.Tonal -> colors.surfaceContainerHigh to colors.onSurface
            PillStyle.Outline -> Color.Transparent to colors.onSurface
        }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = container,
        contentColor = content,
        border = if (style == PillStyle.Outline) BorderStroke(1.5.dp, colors.outline) else null,
        modifier =
            modifier
                .defaultMinSize(minHeight = if (compact) 44.dp else 56.dp)
                .alpha(if (enabled) 1f else 0.4f)
                .semantics { role = Role.Button }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(PaddingValues(horizontal = if (compact) 18.dp else 24.dp, vertical = 10.dp))
        ) {
            if (leadingIcon != null) {
                Icon(painterResource(leadingIcon), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trailingChevron) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** Round icon button, like the soft grey back button in city apps. */
@Composable
fun CircleIconButton(
    @DrawableRes icon: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 0.dp
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = shadowElevation,
        modifier = modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = contentDescription, modifier = Modifier.size(size * 0.5f))
        }
    }
}

/** Floating round control that sits on top of the map. */
@Composable
fun MapControlButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CircleIconButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        size = 52.dp,
        containerColor = MaterialTheme.extended.mapControl,
        shadowElevation = 6.dp
    )
}
