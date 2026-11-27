package io.github.itslanmi.withdrawwise.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.itslanmi.withdrawwise.ui.theme.TileShape

/** Rounded-square tile holding an icon, like the colourful category tiles in city apps. */
@Composable
fun IconTile(
    @DrawableRes icon: Int,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(size)
                .clip(TileShape)
                .background(containerColor)
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(size * 0.54f)
        )
    }
}

/** Rounded-square tile with a short monogram, used as a stand-in for bank logos. */
@Composable
fun MonogramTile(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
    size: Dp = 44.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(size)
                .clip(TileShape)
                .background(containerColor)
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.32f).sp,
            maxLines = 1
        )
    }
}
