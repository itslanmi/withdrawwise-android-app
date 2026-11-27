package io.github.itslanmi.withdrawwise.feature.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.itslanmi.withdrawwise.BuildConfig
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.domain.model.ThemeMode
import io.github.itslanmi.withdrawwise.ui.components.BankMonogram
import io.github.itslanmi.withdrawwise.ui.components.BankOptionList
import io.github.itslanmi.withdrawwise.ui.components.HeroCard
import io.github.itslanmi.withdrawwise.ui.components.LocalBottomBarPadding
import io.github.itslanmi.withdrawwise.ui.components.PillButton
import io.github.itslanmi.withdrawwise.ui.components.PillStyle
import io.github.itslanmi.withdrawwise.ui.components.ScreenHeader
import io.github.itslanmi.withdrawwise.ui.components.SectionLabel
import io.github.itslanmi.withdrawwise.ui.components.SegmentedPills
import io.github.itslanmi.withdrawwise.ui.components.SettingRow
import io.github.itslanmi.withdrawwise.ui.components.SwitchSettingRow
import io.github.itslanmi.withdrawwise.ui.components.WwCard
import io.github.itslanmi.withdrawwise.ui.components.WwTextField
import io.github.itslanmi.withdrawwise.ui.components.hasNotificationPermission
import io.github.itslanmi.withdrawwise.ui.components.rememberPermissionState

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileContent(
        state = state,
        onNameChange = viewModel::setName,
        onBankChange = viewModel::setBank,
        onThemeChange = viewModel::setTheme,
        onWeeklyChange = viewModel::setWeeklySummary,
        onMonthlyChange = viewModel::setMonthlySummary,
        onReset = viewModel::resetApp,
        onPreviewNotification = viewModel::previewNotification,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onNameChange: (String) -> Unit,
    onBankChange: (String) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onWeeklyChange: (Boolean) -> Unit,
    onMonthlyChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onPreviewNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBankPicker by rememberSaveable { mutableStateOf(false) }
    var showNameDialog by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    val prefs = state.preferences
    val notificationsAllowed = rememberPermissionState { hasNotificationPermission() }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(bottom = LocalBottomBarPadding.current)
    ) {
        ScreenHeader(title = stringResource(R.string.nav_profile))
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            IdentityCard(state)

            SectionLabel(stringResource(R.string.profile_section_card), Modifier.padding(top = 12.dp))
            WwCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                val selected = state.selectedBank
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingRow(
                        icon = R.drawable.ic_credit_card,
                        title = selected?.bank?.name ?: stringResource(R.string.profile_pick_bank),
                        subtitle =
                            selected?.let {
                                pluralStringResource(R.plurals.bank_free_atms, it.freeAtmCount, it.freeAtmCount)
                            },
                        onClick = { showBankPicker = true }
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                SettingRow(
                    icon = R.drawable.ic_person,
                    title = stringResource(R.string.profile_name),
                    subtitle = prefs.displayName.ifBlank { stringResource(R.string.profile_name_empty) },
                    onClick = { showNameDialog = true }
                )
            }

            SectionLabel(stringResource(R.string.profile_section_appearance), Modifier.padding(top = 12.dp))
            WwCard(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.profile_theme),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                SegmentedPills(
                    options =
                        listOf(
                            stringResource(R.string.theme_system),
                            stringResource(R.string.theme_light),
                            stringResource(R.string.theme_dark)
                        ),
                    selectedIndex = prefs.themeMode.ordinal,
                    onSelect = { onThemeChange(ThemeMode.entries[it]) }
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val context = LocalContext.current
                WwCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                    SettingRow(
                        icon = R.drawable.ic_language,
                        title = stringResource(R.string.profile_language),
                        subtitle = stringResource(R.string.profile_language_body),
                        onClick = {
                            val intent =
                                Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                                    .setData(Uri.fromParts("package", context.packageName, null))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            SectionLabel(stringResource(R.string.profile_section_notifications), Modifier.padding(top = 12.dp))
            WwCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                SwitchSettingRow(
                    icon = R.drawable.ic_calendar_month,
                    title = stringResource(R.string.profile_weekly_summary),
                    subtitle = stringResource(R.string.profile_weekly_summary_body),
                    checked = prefs.weeklySummary,
                    onCheckedChange = onWeeklyChange
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                SwitchSettingRow(
                    icon = R.drawable.ic_notifications,
                    title = stringResource(R.string.profile_monthly_summary),
                    subtitle = stringResource(R.string.profile_monthly_summary_body),
                    checked = prefs.monthlySummary,
                    onCheckedChange = onMonthlyChange
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                if (notificationsAllowed) {
                    SettingRow(
                        icon = R.drawable.ic_bolt,
                        title = stringResource(R.string.profile_preview_notification),
                        subtitle = stringResource(R.string.profile_preview_notification_body),
                        onClick = onPreviewNotification
                    )
                } else {
                    SettingRow(
                        icon = R.drawable.ic_warning,
                        title = stringResource(R.string.profile_notifications_blocked),
                        subtitle = stringResource(R.string.profile_notifications_blocked_body),
                        trailing = {
                            PillButton(
                                text = stringResource(R.string.permission_allow),
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                compact = true
                            )
                        }
                    )
                }
            }

            SectionLabel(stringResource(R.string.profile_section_about), Modifier.padding(top = 12.dp))
            WwCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                SettingRow(
                    icon = R.drawable.ic_info,
                    title = stringResource(R.string.app_name),
                    subtitle = stringResource(R.string.profile_version, BuildConfig.VERSION_NAME)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                SettingRow(
                    icon = R.drawable.ic_warning,
                    title = stringResource(R.string.profile_demo_data),
                    subtitle = stringResource(R.string.profile_demo_data_body)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                SettingRow(
                    icon = R.drawable.ic_verified,
                    title = stringResource(R.string.profile_licenses),
                    subtitle = stringResource(R.string.profile_licenses_body)
                )
            }

            PillButton(
                text = stringResource(R.string.profile_reset),
                onClick = { showResetDialog = true },
                style = PillStyle.Outline,
                leadingIcon = R.drawable.ic_delete,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showBankPicker) {
        ModalBottomSheet(
            onDismissRequest = { showBankPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
            ) {
                Text(stringResource(R.string.onboarding_bank_title), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                BankOptionList(
                    options = state.banks,
                    selectedId = prefs.bankId,
                    onSelect = {
                        onBankChange(it)
                        showBankPicker = false
                    }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showNameDialog) {
        var draft by rememberSaveable { mutableStateOf(prefs.displayName) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(stringResource(R.string.profile_name)) },
            text = {
                WwTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = stringResource(R.string.onboarding_name_hint),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onNameChange(draft)
                    showNameDialog = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.profile_reset_title)) },
            text = { Text(stringResource(R.string.profile_reset_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    onReset()
                }) { Text(stringResource(R.string.profile_reset_confirm), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }
}

@Composable
private fun IdentityCard(state: ProfileUiState) {
    val name = state.preferences.displayName
    HeroCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f))
            ) {
                Text(
                    text = name.initials().ifEmpty { "WW" },
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (name.isBlank()) {
                        stringResource(
                            R.string.profile_greeting_anonymous
                        )
                    } else {
                        stringResource(R.string.profile_greeting, name)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.profile_withdrawal_count,
                        state.withdrawalCount,
                        state.withdrawalCount
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            state.selectedBank?.let {
                Surface(shape = MaterialTheme.shapes.medium, color = Color.White, shadowElevation = 4.dp) {
                    Box(Modifier.padding(4.dp)) { BankMonogram(it.bank, size = 40.dp) }
                }
            }
        }
    }
}

private fun String.initials(): String = trim()
    .split(Regex("\\s+"))
    .filter { it.isNotEmpty() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
