package io.github.itslanmi.withdrawwise.feature.insights

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.domain.BucketSize
import io.github.itslanmi.withdrawwise.domain.InsightPeriod
import io.github.itslanmi.withdrawwise.domain.Insights
import io.github.itslanmi.withdrawwise.domain.InsightsCalculator
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.ui.components.AmountText
import io.github.itslanmi.withdrawwise.ui.components.BankMonogram
import io.github.itslanmi.withdrawwise.ui.components.BarChart
import io.github.itslanmi.withdrawwise.ui.components.BarEntry
import io.github.itslanmi.withdrawwise.ui.components.EmptyState
import io.github.itslanmi.withdrawwise.ui.components.HeroCard
import io.github.itslanmi.withdrawwise.ui.components.IconTile
import io.github.itslanmi.withdrawwise.ui.components.LocalBottomBarPadding
import io.github.itslanmi.withdrawwise.ui.components.PillButton
import io.github.itslanmi.withdrawwise.ui.components.ScreenHeader
import io.github.itslanmi.withdrawwise.ui.components.SectionLabel
import io.github.itslanmi.withdrawwise.ui.components.SegmentedPills
import io.github.itslanmi.withdrawwise.ui.components.WwCard
import io.github.itslanmi.withdrawwise.ui.theme.extended
import java.time.format.TextStyle

@Composable
fun InsightsScreen(
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottom = LocalBottomBarPadding.current

    if (!state.loading && !state.hasAnyWithdrawals) {
        Column(modifier.fillMaxSize().statusBarsPadding().padding(bottom = bottom)) {
            ScreenHeader(title = stringResource(R.string.nav_insights))
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = R.drawable.ic_bar_chart,
                    title = stringResource(R.string.placeholder_insights_title),
                    message = stringResource(R.string.placeholder_insights_message)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PillButton(
                            text = stringResource(R.string.history_find_atm),
                            onClick = onOpenMap,
                            leadingIcon = R.drawable.ic_map
                        )
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
        return
    }

    val insights = state.insights ?: return
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(bottom = bottom)
    ) {
        ScreenHeader(title = stringResource(R.string.nav_insights))
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SegmentedPills(
                options =
                    listOf(
                        stringResource(R.string.period_week),
                        stringResource(R.string.period_month),
                        stringResource(R.string.period_year)
                    ),
                selectedIndex = state.period.ordinal,
                onSelect = { viewModel.setPeriod(InsightPeriod.entries[it]) }
            )
            FeesHero(insights)
            StatGrid(insights)
            ChartCard(insights, state.metric, viewModel::setMetric)
            SavingsCard(insights)
            if (insights.topAtms.isNotEmpty()) TopAtmsCard(insights, state.banks)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FeesHero(insights: Insights) {
    val periodLabel = periodLabel(insights.period)
    HeroCard(Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.insights_fees_paid, periodLabel),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.85f)
        )
        AmountText(
            cents = insights.current.fees.cents,
            style = MaterialTheme.typography.displayMedium,
            color = Color.White
        )
        Spacer(Modifier.height(10.dp))
        val delta = insights.feesDelta
        val previousExists = insights.previous.count > 0
        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), contentColor = Color.White) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                val icon =
                    when {
                        !previousExists -> R.drawable.ic_calendar_month
                        delta.cents <= 0 -> R.drawable.ic_trending_down
                        else -> R.drawable.ic_trending_up
                    }
                Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                val previous = previousLabel(insights.period)
                val change = Money(kotlin.math.abs(delta.cents)).format()
                Text(
                    text =
                        when {
                            !previousExists -> stringResource(R.string.insights_no_previous, previous)
                            delta.isZero -> stringResource(R.string.insights_same_as, previous)
                            delta.cents < 0 -> stringResource(R.string.insights_less_than, change, previous)
                            else -> stringResource(R.string.insights_more_than, change, previous)
                        },
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun StatGrid(insights: Insights) {
    val totals = insights.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
            StatTile(
                R.drawable.ic_payments,
                stringResource(R.string.insights_withdrawn),
                Modifier.weight(1f).fillMaxHeight()
            ) {
                AmountText(cents = totals.withdrawn.cents, style = MaterialTheme.typography.headlineSmall)
            }
            StatTile(
                R.drawable.ic_receipt_long,
                stringResource(R.string.insights_withdrawals),
                Modifier.weight(1f).fillMaxHeight()
            ) {
                Text(totals.count.toString(), style = MaterialTheme.typography.headlineSmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
            StatTile(
                R.drawable.ic_check_circle,
                stringResource(R.string.insights_free_share),
                Modifier.weight(1f).fillMaxHeight()
            ) {
                Text(
                    "${totals.freePercent} %",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (totals.count > 0) MaterialTheme.extended.feeFree else Color.Unspecified
                )
            }
            StatTile(
                R.drawable.ic_euro,
                stringResource(R.string.insights_average),
                Modifier.weight(1f).fillMaxHeight()
            ) {
                AmountText(cents = totals.average.cents, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun StatTile(
    @DrawableRes icon: Int,
    label: String,
    modifier: Modifier = Modifier,
    value: @Composable () -> Unit
) {
    WwCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        IconTile(
            icon = icon,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            size = 34.dp
        )
        Spacer(Modifier.height(10.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        value()
    }
}

@Composable
private fun ChartCard(insights: Insights, metric: ChartMetric, onMetricChange: (ChartMetric) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val entries =
        insights.buckets.map { bucket ->
            val label =
                when (bucket.size) {
                    BucketSize.Day -> bucket.start.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
                    BucketSize.Month -> bucket.start.month.getDisplayName(TextStyle.SHORT, locale)
                }
            val value = if (metric == ChartMetric.Withdrawn) bucket.withdrawn.cents else bucket.fees.cents
            val title = label.replaceFirstChar { it.titlecase(locale) }
            BarEntry(label = title, value = value, highlighted = bucket.isCurrent)
        }
    val feeColor = MaterialTheme.extended.feeHigh
    WwCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(stringResource(R.string.insights_trend), Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        SegmentedPills(
            options = listOf(stringResource(R.string.insights_withdrawn), stringResource(R.string.insights_fees)),
            selectedIndex = metric.ordinal,
            onSelect = { onMetricChange(ChartMetric.entries[it]) }
        )
        Spacer(Modifier.height(12.dp))
        BarChart(
            entries = entries,
            valueLabel = { Money(it).formatShort() },
            barColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            highlightColor = if (metric == ChartMetric.Fees) feeColor else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SavingsCard(insights: Insights) {
    val avoidable = insights.avoidableFees
    val allFree = insights.current.count > 0 && insights.current.fees.isZero
    WwCard(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.tertiaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(
                icon = if (avoidable.isPositive) R.drawable.ic_lightbulb_filled else R.drawable.ic_savings_filled,
                containerColor = MaterialTheme.extended.feeFree,
                contentColor = MaterialTheme.extended.onFee
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text =
                        when {
                            avoidable.isPositive ->
                                stringResource(R.string.insights_avoidable_title, avoidable.format())

                            allFree -> stringResource(R.string.insights_all_free_title)

                            else -> stringResource(R.string.insights_smart_title)
                        },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text =
                        if (avoidable.isPositive) {
                            pluralStringResource(
                                R.plurals.insights_avoidable_body,
                                insights.avoidableCount,
                                insights.avoidableCount,
                                InsightsCalculator.AvoidableWithinMeters
                            )
                        } else {
                            stringResource(R.string.insights_smart_body)
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun TopAtmsCard(insights: Insights, banks: Map<String, Bank>) {
    val extended = MaterialTheme.extended
    WwCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 12.dp)) {
        SectionLabel(stringResource(R.string.insights_top_atms), Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        insights.topAtms.forEachIndexed { index, usage ->
            if (index >
                0
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                banks[usage.bankId]?.let { BankMonogram(it, size = 38.dp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        usage.atmName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        pluralStringResource(R.plurals.insights_visits, usage.count, usage.count),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(usage.withdrawn.formatShort(), style = MaterialTheme.typography.titleSmall)
                    val feeText =
                        if (usage.fees.isZero) {
                            stringResource(R.string.fee_free)
                        } else {
                            stringResource(R.string.history_fee, usage.fees.format())
                        }
                    Text(
                        feeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (usage.fees.isZero) extended.feeFree else extended.feeHigh
                    )
                }
            }
        }
    }
}

@Composable
private fun periodLabel(period: InsightPeriod): String = stringResource(
    when (period) {
        InsightPeriod.Week -> R.string.period_this_week
        InsightPeriod.Month -> R.string.period_this_month
        InsightPeriod.Year -> R.string.period_this_year
    }
)

@Composable
private fun previousLabel(period: InsightPeriod): String = stringResource(
    when (period) {
        InsightPeriod.Week -> R.string.period_previous_week
        InsightPeriod.Month -> R.string.period_previous_month
        InsightPeriod.Year -> R.string.period_previous_year
    }
)
