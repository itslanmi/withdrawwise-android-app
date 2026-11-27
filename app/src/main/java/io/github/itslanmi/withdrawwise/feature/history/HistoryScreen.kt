package io.github.itslanmi.withdrawwise.feature.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.feature.log.AtmPickerSheet
import io.github.itslanmi.withdrawwise.feature.log.LogWithdrawalSheet
import io.github.itslanmi.withdrawwise.ui.components.AmountText
import io.github.itslanmi.withdrawwise.ui.components.BankMonogram
import io.github.itslanmi.withdrawwise.ui.components.EmptyState
import io.github.itslanmi.withdrawwise.ui.components.LocalBottomBarPadding
import io.github.itslanmi.withdrawwise.ui.components.MonogramTile
import io.github.itslanmi.withdrawwise.ui.components.PillButton
import io.github.itslanmi.withdrawwise.ui.components.PillStyle
import io.github.itslanmi.withdrawwise.ui.components.ScreenHeader
import io.github.itslanmi.withdrawwise.ui.components.WwCard
import io.github.itslanmi.withdrawwise.ui.theme.extended
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(onOpenMap: () -> Unit, modifier: Modifier = Modifier, viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var picking by rememberSaveable { mutableStateOf(false) }
    var loggingAtmId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val deletedMessage = stringResource(R.string.history_deleted)
    val undoLabel = stringResource(R.string.action_undo)
    val bottomPadding = LocalBottomBarPadding.current

    fun deleteWithUndo(item: WithdrawalItem) {
        viewModel.delete(item.withdrawal)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
        }
    }

    Box(modifier.fillMaxSize()) {
        if (state.isEmpty) {
            Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom = bottomPadding)) {
                ScreenHeader(title = stringResource(R.string.nav_history))
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = R.drawable.ic_receipt_long,
                        title = stringResource(R.string.placeholder_history_title),
                        message = stringResource(R.string.placeholder_history_message)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PillButton(
                                text = stringResource(R.string.history_log_first),
                                onClick = { picking = true },
                                leadingIcon = R.drawable.ic_add
                            )
                            Spacer(Modifier.height(4.dp))
                            TextButton(onClick = onOpenMap) { Text(stringResource(R.string.history_find_atm)) }
                            TextButton(onClick = viewModel::loadSampleData) {
                                Text(
                                    stringResource(R.string.history_load_sample),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            HistoryList(
                state = state,
                bottomPadding = bottomPadding,
                onEdit = { editingId = it.withdrawal.id },
                onDelete = ::deleteWithUndo
            )
            PillButton(
                text = stringResource(R.string.history_log),
                onClick = { picking = true },
                style = PillStyle.Accent,
                leadingIcon = R.drawable.ic_add,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = bottomPadding)
            )
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = bottomPadding + 64.dp)
        )
    }

    if (picking) {
        AtmPickerSheet(
            atms = state.atms,
            onDismiss = { picking = false },
            onPick = {
                picking = false
                loggingAtmId = it.id
            }
        )
    }

    loggingAtmId?.let { id ->
        state.atms.firstOrNull { it.id == id }?.let { atm ->
            LogWithdrawalSheet(
                atm = atm,
                quoteFor = { amount -> state.calculator.quote(state.cardBankId, atm.atm, amount) },
                onDismiss = { loggingAtmId = null },
                onConfirm = { amount ->
                    viewModel.log(atm.id, amount)
                    loggingAtmId = null
                }
            )
        }
    }

    editingId?.let { id ->
        val item = state.groups.flatMap { it.items }.firstOrNull { it.withdrawal.id == id }
        val atm = item?.let { found -> state.atms.firstOrNull { it.id == found.withdrawal.atmId } }
        if (item != null && atm != null) {
            LogWithdrawalSheet(
                atm = atm,
                quoteFor = { amount -> state.calculator.quote(state.cardBankId, atm.atm, amount) },
                onDismiss = { editingId = null },
                onConfirm = { amount ->
                    viewModel.updateAmount(id, amount)
                    editingId = null
                },
                initialAmount = item.withdrawal.amount,
                onDelete = {
                    editingId = null
                    deleteWithUndo(item)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryList(
    state: HistoryUiState,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onEdit: (WithdrawalItem) -> Unit,
    onDelete: (WithdrawalItem) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = bottomPadding + 72.dp),
        modifier = Modifier.fillMaxSize().statusBarsPadding()
    ) {
        item {
            ScreenHeader(
                title = stringResource(R.string.nav_history),
                subtitle = pluralStringResource(R.plurals.profile_withdrawal_count, state.count, state.count)
            )
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                SummaryTile(
                    stringResource(R.string.history_this_month_withdrawn),
                    state.thisMonthTotal,
                    Modifier.weight(1f)
                )
                SummaryTile(
                    stringResource(R.string.history_this_month_fees),
                    state.thisMonthFees,
                    Modifier.weight(1f),
                    highlightFees = true
                )
            }
        }
        state.groups.forEach { group ->
            stickyHeader(key = "header-${group.month}") {
                MonthHeader(group)
            }
            items(group.items, key = { it.withdrawal.id }) { item ->
                SwipeableWithdrawalRow(item = item, onClick = { onEdit(item) }, onDelete = { onDelete(item) })
            }
        }
    }
}

@Composable
private fun SummaryTile(label: String, value: Money, modifier: Modifier = Modifier, highlightFees: Boolean = false) {
    WwCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        AmountText(
            cents = value.cents,
            style = MaterialTheme.typography.headlineMedium,
            color =
                when {
                    !highlightFees -> MaterialTheme.colorScheme.onSurface
                    value.isZero -> MaterialTheme.extended.feeFree
                    else -> MaterialTheme.extended.feeHigh
                }
        )
    }
}

@Composable
private fun MonthHeader(group: MonthGroup) {
    val locale = LocalConfiguration.current.locales[0]
    val monthName = group.month.month.getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar {
        it.titlecase(locale)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(top = 10.dp)
    ) {
        Text(
            "$monthName ${group.month.year}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            stringResource(R.string.history_month_summary, group.total.formatShort(), group.fees.format()),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SwipeableWithdrawalRow(item: WithdrawalItem, onClick: () -> Unit, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        onDismiss = { if (it == SwipeToDismissBoxValue.EndToStart) onDelete() },
        backgroundContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(
                            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                MaterialTheme.extended.feeHigh
                            } else {
                                androidx.compose.ui.graphics.Color.Transparent
                            }
                        )
                        .padding(horizontal = 20.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.log_delete),
                    tint = MaterialTheme.extended.onFee
                )
            }
        }
    ) {
        WithdrawalRow(item, onClick)
    }
}

@Composable
private fun WithdrawalRow(item: WithdrawalItem, onClick: () -> Unit) {
    val withdrawal = item.withdrawal
    val locale = LocalConfiguration.current.locales[0]
    val time =
        remember(item.dateTime, locale) {
            item.dateTime.format(DateTimeFormatter.ofPattern("EEE d MMM · HH:mm", locale))
        }
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
            if (item.bank != null) {
                BankMonogram(item.bank, size = 42.dp)
            } else {
                MonogramTile("?", MaterialTheme.colorScheme.outline, size = 42.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    withdrawal.atmName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(withdrawal.amount.formatShort(), style = MaterialTheme.typography.titleMedium)
                if (withdrawal.fee.isZero) {
                    Text(
                        stringResource(R.string.fee_free),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.extended.feeFree
                    )
                } else {
                    Text(
                        stringResource(R.string.history_fee, withdrawal.fee.format()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.extended.feeHigh
                    )
                }
            }
        }
    }
}
