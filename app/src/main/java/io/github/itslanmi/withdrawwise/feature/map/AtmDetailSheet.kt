package io.github.itslanmi.withdrawwise.feature.map

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.domain.FeeCalculator
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.ui.components.AmountText
import io.github.itslanmi.withdrawwise.ui.components.BankMonogram
import io.github.itslanmi.withdrawwise.ui.components.CircleIconButton
import io.github.itslanmi.withdrawwise.ui.components.IconTile
import io.github.itslanmi.withdrawwise.ui.components.PillButton
import io.github.itslanmi.withdrawwise.ui.components.PillStyle
import io.github.itslanmi.withdrawwise.ui.components.WwCard
import io.github.itslanmi.withdrawwise.ui.components.color
import io.github.itslanmi.withdrawwise.ui.model.formatDistance
import io.github.itslanmi.withdrawwise.ui.theme.extended

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtmDetailSheet(
    selection: AtmSelection,
    cardBank: Bank?,
    onDismiss: () -> Unit,
    onShowAtm: (String) -> Unit,
    modifier: Modifier = Modifier,
    onLogWithdrawal: ((String) -> Unit)? = null
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        AtmDetailContent(selection, cardBank, onDismiss, onShowAtm, onLogWithdrawal)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AtmDetailContent(
    selection: AtmSelection,
    cardBank: Bank?,
    onClose: () -> Unit,
    onShowAtm: (String) -> Unit,
    onLogWithdrawal: ((String) -> Unit)?
) {
    val atm = selection.atm
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
            .padding(bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BankMonogram(atm.bank, size = 52.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(atm.atm.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    "${atm.atm.address} · ${atm.atm.neighbourhood}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CircleIconButton(
                icon = R.drawable.ic_close,
                contentDescription = stringResource(R.string.action_close),
                onClick = onClose
            )
        }

        Spacer(Modifier.height(20.dp))
        WwCard(Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.atm_fee_label, FeeCalculator.TypicalAmount.formatShort()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            if (atm.isFree) {
                Text(
                    stringResource(R.string.fee_free),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.extended.feeFree
                )
            } else {
                AmountText(
                    cents = atm.quote.total.cents,
                    style = MaterialTheme.typography.displaySmall,
                    color = atm.quote.tier.color()
                )
            }
            Text(
                text =
                    when {
                        cardBank == null -> stringResource(R.string.atm_fee_no_bank)

                        atm.bank.id == cardBank.id -> stringResource(R.string.atm_fee_own_bank, cardBank.shortName)

                        atm.isFree -> stringResource(R.string.atm_fee_partner, cardBank.shortName)

                        atm.quote.surcharge.isPositive ->
                            stringResource(
                                R.string.atm_fee_with_surcharge,
                                cardBank.shortName,
                                atm.quote.surcharge.format()
                            )

                        else -> stringResource(R.string.atm_fee_other_bank, cardBank.shortName)
                    },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            atm.distanceMeters?.let { InfoChip(R.drawable.ic_distance, formatDistance(it)) }
            InfoChip(
                R.drawable.ic_schedule,
                stringResource(if (atm.atm.isOpen24h) R.string.atm_open_24h else R.string.atm_limited_hours)
            )
            if (atm.atm.hasContactless) InfoChip(R.drawable.ic_credit_card, stringResource(R.string.atm_contactless))
        }

        val alternative = selection.freeAlternative
        if (alternative != null) {
            Spacer(Modifier.height(12.dp))
            WwCard(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(
                        icon = R.drawable.ic_lightbulb_filled,
                        containerColor = MaterialTheme.extended.feeFree,
                        contentColor = MaterialTheme.extended.onFee,
                        size = 40.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.atm_tip_title, atm.quote.total.format()),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            stringResource(
                                R.string.atm_tip_body,
                                alternative.atm.name,
                                formatDistance(selection.freeAlternativeMeters ?: 0.0)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    TextButton(onClick = { onShowAtm(alternative.id) }) {
                        Text(
                            stringResource(R.string.atm_tip_show),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PillButton(
                text = stringResource(R.string.atm_navigate),
                onClick = {
                    val location = atm.atm.location
                    val destination = "${location.latitude},${location.longitude}"
                    val uri = "https://www.google.com/maps/dir/?api=1&destination=$destination&travelmode=walking"
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
                },
                style = if (onLogWithdrawal == null) PillStyle.Primary else PillStyle.Outline,
                leadingIcon = R.drawable.ic_directions,
                modifier = Modifier.weight(1f)
            )
            if (onLogWithdrawal != null) {
                PillButton(
                    text = stringResource(R.string.atm_log),
                    onClick = { onLogWithdrawal(atm.id) },
                    leadingIcon = R.drawable.ic_add,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoChip(@DrawableRes icon: Int, text: String) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}
