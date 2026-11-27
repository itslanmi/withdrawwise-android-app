package io.github.itslanmi.withdrawwise.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.ui.components.BankOptionRow
import io.github.itslanmi.withdrawwise.ui.components.CircleIconButton
import io.github.itslanmi.withdrawwise.ui.components.IconTile
import io.github.itslanmi.withdrawwise.ui.components.LocationPermissions
import io.github.itslanmi.withdrawwise.ui.components.PermissionCard
import io.github.itslanmi.withdrawwise.ui.components.PillButton
import io.github.itslanmi.withdrawwise.ui.components.WwTextField
import io.github.itslanmi.withdrawwise.ui.components.hasLocationPermission
import io.github.itslanmi.withdrawwise.ui.components.hasNotificationPermission
import io.github.itslanmi.withdrawwise.ui.components.rememberPermissionState
import io.github.itslanmi.withdrawwise.ui.theme.extended

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier, viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnboardingContent(
        state = state,
        onNext = viewModel::next,
        onBack = viewModel::back,
        onSelectBank = viewModel::selectBank,
        onNameChange = viewModel::setName,
        onFinish = viewModel::finish,
        modifier = modifier
    )
}

@Composable
private fun OnboardingContent(
    state: OnboardingUiState,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSelectBank: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = state.step != OnboardingStep.Welcome, onBack = onBack)
    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        TopBar(step = state.step, onBack = onBack)

        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (slideInHorizontally { if (forward) it / 3 else -it / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { if (forward) -it / 3 else it / 3 } + fadeOut())
            },
            label = "onboarding-step",
            modifier = Modifier.weight(1f)
        ) { step ->
            when (step) {
                OnboardingStep.Welcome -> WelcomeStep()
                OnboardingStep.Bank -> BankStep(state, onSelectBank)
                OnboardingStep.Finish -> FinishStep(state.name, onNameChange)
            }
        }

        val ctaText =
            when (state.step) {
                OnboardingStep.Welcome -> stringResource(R.string.onboarding_get_started)
                OnboardingStep.Bank -> stringResource(R.string.onboarding_continue)
                OnboardingStep.Finish -> stringResource(R.string.onboarding_finish)
            }
        PillButton(
            text = ctaText,
            onClick = if (state.step == OnboardingStep.Finish) onFinish else onNext,
            enabled = state.canContinue,
            trailingChevron = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun TopBar(step: OnboardingStep, onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 20.dp)
    ) {
        Box(Modifier.size(44.dp)) {
            if (step != OnboardingStep.Welcome) {
                CircleIconButton(
                    icon = R.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onBack
                )
            }
        }
        Spacer(Modifier.weight(1f))
        val colors = MaterialTheme.colorScheme
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OnboardingStep.entries.forEach { item ->
                val active = item.ordinal <= step.ordinal
                val color by animateColorAsState(
                    if (active) colors.onSurface else colors.surfaceContainerHighest,
                    label = "progress"
                )
                Box(
                    Modifier
                        .size(width = if (item == step) 28.dp else 10.dp, height = 10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)
    ) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            WelcomeHero()
        }
        Text(
            stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.onboarding_welcome_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
    }
}

/** Big ATM tile on a glowing ember disc, with fee chips floating around it. */
@Composable
private fun WelcomeHero() {
    val transition = rememberInfiniteTransition(label = "hero")
    val float by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "float"
    )
    val extended = MaterialTheme.extended
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(320.dp)) {
        Box(
            Modifier
                .size(320.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), Color.Transparent)
                    )
                )
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(196.dp)
                    .clip(CircleShape)
                    .background(extended.brandGradient)
        ) {
            Icon(
                painterResource(R.drawable.ic_euro),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(104.dp)
            )
        }
        FeeChip(
            text = stringResource(R.string.fee_free),
            dot = extended.feeFree,
            modifier = Modifier.offset(x = (-92).dp, y = (-104).dp).graphicsLayer { translationY = float }
        )
        FeeChip(
            text = "2,95 €",
            dot = extended.feeHigh,
            modifier = Modifier.offset(x = 104.dp, y = (-40).dp).graphicsLayer { translationY = -float }
        )
        IconTile(
            icon = R.drawable.ic_savings_filled,
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            size = 56.dp,
            modifier = Modifier.offset(x = (-104).dp, y = 96.dp).graphicsLayer { translationY = -float }
        )
        FeeChip(
            text = "0,90 €",
            dot = extended.feeLow,
            modifier = Modifier.offset(x = 92.dp, y = 112.dp).graphicsLayer { translationY = float }
        )
    }
}

@Composable
private fun FeeChip(text: String, dot: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun BankStep(state: OnboardingUiState, onSelectBank: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            StepTitle(
                title = stringResource(R.string.onboarding_bank_title),
                body = stringResource(R.string.onboarding_bank_body)
            )
        }
        items(state.banks, key = { it.bank.id }) { option ->
            BankOptionRow(
                option = option,
                selected = option.bank.id == state.selectedBankId,
                onClick = { onSelectBank(option.bank.id) }
            )
        }
    }
}

@Composable
private fun FinishStep(name: String, onNameChange: (String) -> Unit) {
    val locationGranted = rememberPermissionState { hasLocationPermission() }
    val notificationsGranted = rememberPermissionState { hasNotificationPermission() }
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().verticalScroll(
            rememberScrollState()
        ).padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        StepTitle(
            title = stringResource(R.string.onboarding_finish_title),
            body = stringResource(R.string.onboarding_finish_body)
        )
        WwTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = stringResource(R.string.onboarding_name_hint),
            leadingIcon = R.drawable.ic_person,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            )
        )
        Spacer(Modifier.height(4.dp))
        PermissionCard(
            icon = R.drawable.ic_my_location,
            title = stringResource(R.string.permission_location_title),
            body = stringResource(R.string.permission_location_body),
            granted = locationGranted,
            onRequest = { locationLauncher.launch(LocationPermissions) }
        )
        PermissionCard(
            icon = R.drawable.ic_notifications,
            title = stringResource(R.string.permission_notifications_title),
            body = stringResource(R.string.permission_notifications_body),
            granted = notificationsGranted,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )
        Text(
            stringResource(R.string.onboarding_change_later),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StepTitle(title: String, body: String) {
    Column(Modifier.padding(bottom = 8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
