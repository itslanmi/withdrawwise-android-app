package io.github.itslanmi.withdrawwise.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.domain.model.Bank

/** A bank the user can pick, plus how many ATMs in the city are free with its card. */
data class BankOption(val bank: Bank, val freeAtmCount: Int)

val Bank.color: Color get() = Color(colorArgb)

@Composable
fun BankMonogram(bank: Bank, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    MonogramTile(text = bank.shortName, containerColor = bank.color, size = size, modifier = modifier)
}

@Composable
fun BankOptionRow(option: BankOption, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val border by animateColorAsState(if (selected) colors.onSurface else Color.Transparent, label = "bank-border")
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainerLow,
        border = BorderStroke(2.dp, border),
        modifier =
            modifier.fillMaxWidth().semantics {
                role = Role.RadioButton
                this.selected = selected
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
            BankMonogram(option.bank)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(option.bank.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    pluralStringResource(R.plurals.bank_free_atms, option.freeAtmCount, option.freeAtmCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            Icon(
                painterResource(
                    if (selected) R.drawable.ic_check_circle_filled else R.drawable.ic_radio_button_unchecked
                ),
                contentDescription = null,
                tint = if (selected) colors.onSurface else colors.outline,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun BankOptionList(
    options: List<BankOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = modifier) {
        options.forEach { option ->
            BankOptionRow(option = option, selected = option.bank.id == selectedId, onClick = {
                onSelect(option.bank.id)
            })
        }
    }
}

/** Card-issuing banks, most useful first (the ones with the most free ATMs in the city). */
fun buildBankOptions(
    banks: List<Bank>,
    atms: List<io.github.itslanmi.withdrawwise.domain.model.Atm>
): List<BankOption> {
    val calculator = io.github.itslanmi.withdrawwise.domain.FeeCalculator(banks)
    return banks
        .filter { it.issuesCards }
        .map { bank -> BankOption(bank, atms.count { calculator.isFree(bank.id, it) }) }
        .sortedByDescending { it.freeAtmCount }
}
