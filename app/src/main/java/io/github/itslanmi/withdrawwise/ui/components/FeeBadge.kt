package io.github.itslanmi.withdrawwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.domain.FeeQuote
import io.github.itslanmi.withdrawwise.domain.FeeTier
import io.github.itslanmi.withdrawwise.ui.theme.extended

@Composable
fun FeeTier.color(): Color = when (this) {
    FeeTier.Free -> MaterialTheme.extended.feeFree
    FeeTier.Low -> MaterialTheme.extended.feeLow
    FeeTier.High -> MaterialTheme.extended.feeHigh
}

/** Small pill showing what an ATM costs: a coloured dot and "Free" or the amount. */
@Composable
fun FeeBadge(quote: FeeQuote, modifier: Modifier = Modifier) {
    val color = quote.tier.color()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .clip(CircleShape)
                .background(color.copy(alpha = 0.14f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (quote.total.isZero) stringResource(R.string.fee_free) else quote.total.formatShort(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
