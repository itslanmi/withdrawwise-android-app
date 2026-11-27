package io.github.itslanmi.withdrawwise.feature.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.ui.theme.TileShape

/**
 * Map marker: a rounded square in the fee colour with a euro glyph and a small pointer.
 * Rendered to a bitmap by maps-compose, so it must not depend on animations.
 */
@Composable
fun AtmPin(color: Color, selected: Boolean, outline: Color, modifier: Modifier = Modifier) {
    val size = if (selected) 46.dp else 34.dp
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(size)
                    .clip(TileShape)
                    .background(color)
                    .border(if (selected) 3.dp else 2.dp, outline, TileShape)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_euro),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.52f)
            )
        }
        Canvas(Modifier.size(width = 12.dp, height = 7.dp)) {
            val path =
                Path().apply {
                    moveTo(0f, 0f)
                    lineTo(this@Canvas.size.width, 0f)
                    lineTo(this@Canvas.size.width / 2, this@Canvas.size.height)
                    close()
                }
            drawPath(path, color)
        }
    }
}
