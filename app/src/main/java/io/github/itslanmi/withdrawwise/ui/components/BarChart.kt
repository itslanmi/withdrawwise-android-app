package io.github.itslanmi.withdrawwise.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class BarEntry(val label: String, val value: Long, val highlighted: Boolean)

/**
 * Rounded bar chart drawn on a canvas. Bars grow in with a spring whenever the data changes;
 * the highlighted bar (usually "now") uses the accent colour and shows its value on top.
 */
@Composable
fun BarChart(
    entries: List<BarEntry>,
    valueLabel: (Long) -> String,
    barColor: Color,
    highlightColor: Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 168.dp
) {
    val max = (entries.maxOfOrNull { it.value } ?: 0L).coerceAtLeast(1L)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(entries) {
        progress.snapTo(0f)
        progress.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
    }
    val colors = MaterialTheme.colorScheme
    val gridColor = colors.outlineVariant
    val emptyColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlighted = entries.lastOrNull { it.highlighted }
    val description = entries.joinToString { "${it.label}: ${valueLabel(it.value)}" }

    Column(modifier.semantics { contentDescription = description }) {
        Text(
            text = highlighted?.let { valueLabel(it.value) }.orEmpty(),
            style = MaterialTheme.typography.labelLarge,
            color = highlightColor,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Canvas(Modifier.fillMaxWidth().height(chartHeight)) {
            val count = entries.size.coerceAtLeast(1)
            val slot = size.width / count
            val barWidth = (slot * 0.56f).coerceAtMost(36.dp.toPx())
            val radius = CornerRadius(barWidth / 2, barWidth / 2)

            // Dashed guide lines at 50 % and 100 %.
            listOf(0f, 0.5f).forEach { fraction ->
                val y = size.height * fraction
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
            }

            entries.forEachIndexed { index, entry ->
                val left = slot * index + (slot - barWidth) / 2
                val minHeight = barWidth
                val height =
                    if (entry.value == 0L) {
                        minHeight * 0.35f
                    } else {
                        (size.height * entry.value / max * progress.value).coerceAtLeast(minHeight)
                    }
                drawRoundRect(
                    color =
                        when {
                            entry.value == 0L -> emptyColor
                            entry.highlighted -> highlightColor
                            else -> barColor
                        },
                    topLeft = Offset(left, size.height - height),
                    size = Size(barWidth, height),
                    cornerRadius = radius
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            entries.forEach { entry ->
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (entry.highlighted) colors.onSurface else colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
