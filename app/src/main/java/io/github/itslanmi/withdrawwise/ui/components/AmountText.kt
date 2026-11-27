package io.github.itslanmi.withdrawwise.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Renders an amount in the "14⁶⁵ €" style: big whole euros, small raised cents.
 *
 * @param cents amount in euro cents.
 */
@Composable
fun AmountText(
    cents: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    showSign: Boolean = false
) {
    val text =
        remember(cents, showSign) {
            val whole = abs(cents) / 100
            val fraction = abs(cents) % 100
            val wholeText = NumberFormat.getIntegerInstance(Locale.GERMANY).format(whole)
            buildAnnotatedString {
                if (cents < 0) {
                    append("−")
                } else if (showSign && cents > 0) {
                    append("+")
                }
                append(wholeText)
                withStyle(SpanStyle(fontSize = 0.5.em, baselineShift = BaselineShift(0.75f))) {
                    append(fraction.toString().padStart(2, '0'))
                }
                withStyle(SpanStyle(fontSize = 0.62.em)) { append("\u2009€") }
            }
        }
    Text(text = text, modifier = modifier, style = style, color = color, maxLines = 1)
}
