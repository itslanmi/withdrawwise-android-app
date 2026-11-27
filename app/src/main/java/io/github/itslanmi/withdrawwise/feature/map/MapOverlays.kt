package io.github.itslanmi.withdrawwise.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.domain.FeeTier
import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.ui.components.AmountText
import io.github.itslanmi.withdrawwise.ui.components.BankMonogram
import io.github.itslanmi.withdrawwise.ui.components.FeeBadge
import io.github.itslanmi.withdrawwise.ui.components.color
import io.github.itslanmi.withdrawwise.ui.model.AtmUi
import io.github.itslanmi.withdrawwise.ui.model.formatDistance
import io.github.itslanmi.withdrawwise.ui.theme.extended

@Composable
fun MapSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    Surface(
        shape = CircleShape,
        color = MaterialTheme.extended.mapControl,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth().height(54.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 18.dp, end = 6.dp)) {
            Icon(
                painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(12.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(
                        stringResource(R.string.map_search_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    onQueryChange("")
                    focusManager.clearFocus()
                }) {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.action_clear)
                    )
                }
            }
        }
    }
}

@Composable
fun MapFilterChips(selected: AtmFilter, onSelect: (AtmFilter) -> Unit, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)
    ) {
        AtmFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            val colors = MaterialTheme.colorScheme
            Surface(
                onClick = { onSelect(filter) },
                shape = CircleShape,
                color = if (isSelected) colors.inverseSurface else MaterialTheme.extended.mapControl,
                contentColor = if (isSelected) colors.inverseOnSurface else colors.onSurface,
                shadowElevation = 4.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    val dot =
                        when (filter) {
                            AtmFilter.Free -> FeeTier.Free.color()
                            AtmFilter.LowFee -> FeeTier.Low.color()
                            else -> null
                        }
                    if (dot != null) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
                        Spacer(Modifier.width(8.dp))
                    } else {
                        val icon = if (filter == AtmFilter.Open24h) R.drawable.ic_schedule else R.drawable.ic_map
                        Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(stringResource(filter.label()), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

private fun AtmFilter.label(): Int = when (this) {
    AtmFilter.All -> R.string.map_filter_all
    AtmFilter.Free -> R.string.map_filter_free
    AtmFilter.LowFee -> R.string.map_filter_low
    AtmFilter.Open24h -> R.string.map_filter_24h
}

@Composable
fun SearchResultsCard(results: List<AtmUi>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.extended.mapControl,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(vertical = 6.dp)) {
            if (results.isEmpty()) {
                Text(
                    stringResource(R.string.map_search_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            results.forEachIndexed { index, atm ->
                if (index >
                    0
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 70.dp)
                    )
                }
                AtmListRow(atm = atm, onClick = { onSelect(atm.id) })
            }
        }
    }
}

/** Compact ATM row used by search results and the ATM picker. */
@Composable
fun AtmListRow(atm: AtmUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            BankMonogram(atm.bank, size = 40.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    atm.atm.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val distance = atm.distanceMeters?.let { " · ${formatDistance(it)}" }.orEmpty()
                Text(
                    "${atm.atm.neighbourhood}$distance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            FeeBadge(atm.quote)
        }
    }
}

/** Floating pill with this month's fees, in the spirit of a transit app's balance pill. */
@Composable
fun MonthFeesPill(fees: Money, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.extended.mapControl,
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(PaddingValues(start = 6.dp, end = 18.dp, top = 6.dp, bottom = 6.dp))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.extended.brandGradient)
            ) {
                Icon(
                    painterResource(R.drawable.ic_payments),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    stringResource(R.string.map_fees_this_month),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AmountText(cents = fees.cents, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** Shown in builds without a Google Maps key, where tiles and markers cannot load. */
@Composable
fun MissingMapKeyNotice(modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.extended.mapControl,
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
            io.github.itslanmi.withdrawwise.ui.components.IconTile(
                icon = R.drawable.ic_warning,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.map_key_missing_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.map_key_missing_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
