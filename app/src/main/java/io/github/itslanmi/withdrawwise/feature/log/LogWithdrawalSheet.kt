package io.github.itslanmi.withdrawwise.feature.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.domain.FeeQuote
import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.ui.components.BankMonogram
import io.github.itslanmi.withdrawwise.ui.components.CircleIconButton
import io.github.itslanmi.withdrawwise.ui.components.PillButton
import io.github.itslanmi.withdrawwise.ui.components.PillStyle
import io.github.itslanmi.withdrawwise.ui.components.SegmentedPills
import io.github.itslanmi.withdrawwise.ui.components.WwCard
import io.github.itslanmi.withdrawwise.ui.components.color
import io.github.itslanmi.withdrawwise.ui.model.AtmUi
import io.github.itslanmi.withdrawwise.ui.theme.extended

private val QuickAmounts = listOf(20, 50, 100, 200)
private const val MaxEuros = 1_000

/**
 * Sheet for entering a withdrawal amount with a live fee preview. Used both for logging a new
 * withdrawal and for editing an existing one (pass [initialAmount] and [onDelete]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWithdrawalSheet(
    atm: AtmUi,
    quoteFor: (Money) -> FeeQuote,
    onDismiss: () -> Unit,
    onConfirm: (Money) -> Unit,
    modifier: Modifier = Modifier,
    initialAmount: Money? = null,
    onDelete: (() -> Unit)? = null
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        LogWithdrawalContent(atm, quoteFor, onDismiss, onConfirm, initialAmount, onDelete)
    }
}

@Composable
private fun LogWithdrawalContent(
    atm: AtmUi,
    quoteFor: (Money) -> FeeQuote,
    onClose: () -> Unit,
    onConfirm: (Money) -> Unit,
    initialAmount: Money?,
    onDelete: (() -> Unit)?
) {
    var digits by rememberSaveable { mutableStateOf(initialAmount?.let { (it.cents / 100).toString() } ?: "50") }
    val euros = digits.toIntOrNull() ?: 0
    val tooMuch = euros > MaxEuros
    val valid = euros in 1..MaxEuros
    val amount = Money.euros(euros.coerceIn(0, MaxEuros))
    val quote = quoteFor(amount)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(if (onDelete == null) R.string.log_title else R.string.log_edit_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            CircleIconButton(
                icon = R.drawable.ic_close,
                contentDescription = stringResource(R.string.action_close),
                onClick = onClose
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BankMonogram(atm.bank, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(atm.atm.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    atm.atm.neighbourhood,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicTextField(
                value = digits,
                onValueChange = { input -> digits = input.filter(Char::isDigit).take(5).trimStart('0') },
                singleLine = true,
                textStyle =
                    MaterialTheme.typography.displayLarge.copy(
                        color = if (tooMuch) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.width(IntrinsicSize.Min),
                decorationBox = { inner ->
                    if (digits.isEmpty()) {
                        Text(
                            "0",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    inner()
                }
            )
            Spacer(Modifier.width(8.dp))
            Text("€", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (tooMuch) {
            Text(
                stringResource(R.string.log_amount_too_high, MaxEuros),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))
        SegmentedPills(
            options = QuickAmounts.map { "$it €" },
            selectedIndex = QuickAmounts.indexOf(euros),
            onSelect = { digits = QuickAmounts[it].toString() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(20.dp))
        WwCard(Modifier.fillMaxWidth()) {
            PreviewRow(
                label = stringResource(R.string.log_fee),
                value = if (quote.total.isZero) stringResource(R.string.fee_free) else quote.total.format(),
                valueColor = if (quote.total.isZero) MaterialTheme.extended.feeFree else quote.tier.color()
            )
            Spacer(Modifier.height(8.dp))
            PreviewRow(
                label = stringResource(R.string.log_total),
                value = (amount + quote.total).format(),
                valueColor = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(20.dp))
        PillButton(
            text =
                if (onDelete == null) {
                    stringResource(R.string.log_confirm, amount.formatShort())
                } else {
                    stringResource(R.string.action_save)
                },
            onClick = { onConfirm(amount) },
            enabled = valid,
            leadingIcon = R.drawable.ic_check,
            modifier = Modifier.fillMaxWidth()
        )
        if (onDelete != null) {
            Spacer(Modifier.height(10.dp))
            PillButton(
                text = stringResource(R.string.log_delete),
                onClick = onDelete,
                style = PillStyle.Outline,
                leadingIcon = R.drawable.ic_delete,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor)
    }
}
