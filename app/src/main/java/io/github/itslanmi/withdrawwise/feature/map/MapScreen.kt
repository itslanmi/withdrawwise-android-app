package io.github.itslanmi.withdrawwise.feature.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import io.github.itslanmi.withdrawwise.BuildConfig
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.domain.model.GeoPoint
import io.github.itslanmi.withdrawwise.feature.log.LogWithdrawalSheet
import io.github.itslanmi.withdrawwise.ui.components.LocalBottomBarPadding
import io.github.itslanmi.withdrawwise.ui.components.LocationPermissions
import io.github.itslanmi.withdrawwise.ui.components.MapControlButton
import io.github.itslanmi.withdrawwise.ui.components.PillButton
import io.github.itslanmi.withdrawwise.ui.components.color
import io.github.itslanmi.withdrawwise.ui.components.hasLocationPermission
import io.github.itslanmi.withdrawwise.ui.components.rememberPermissionState
import io.github.itslanmi.withdrawwise.ui.model.LjubljanaCenter
import io.github.itslanmi.withdrawwise.ui.theme.extended

@Composable
fun MapScreen(onOpenInsights: () -> Unit, modifier: Modifier = Modifier, viewModel: MapViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isDark = MaterialTheme.extended.isDark
    val hasLocation = rememberPermissionState { hasLocationPermission() }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) viewModel.refreshLocation(focus = true)
        }

    val snackbar = remember { SnackbarHostState() }
    var loggingAtmId by rememberSaveable { mutableStateOf<String?>(null) }
    val loggedTemplate = stringResource(R.string.map_logged)
    val freeLabel = stringResource(R.string.fee_free)

    val cameraState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LjubljanaCenter.toLatLng(), MapViewModel.CityZoom)
        }

    LaunchedEffect(hasLocation) { if (hasLocation) viewModel.refreshLocation() }
    LaunchedEffect(Unit) {
        viewModel.mapEvents.collect { event ->
            when (event) {
                is MapEvent.FocusOn ->
                    cameraState.animate(CameraUpdateFactory.newLatLngZoom(event.point.toLatLng(), event.zoom), 650)

                is MapEvent.Logged -> {
                    val fee = event.withdrawal.fee
                    snackbar.showSnackbar(
                        loggedTemplate.format(
                            event.withdrawal.amount.formatShort(),
                            if (fee.isZero) freeLabel else fee.format()
                        )
                    )
                }
            }
        }
    }

    val mapStyle =
        remember(isDark) {
            MapStyleOptions.loadRawResourceStyle(context, if (isDark) R.raw.map_style_dark else R.raw.map_style_light)
        }
    val bottomPadding = LocalBottomBarPadding.current
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val selectedId = state.selection?.atm?.id
    val outline = MaterialTheme.colorScheme.surface

    Box(modifier.fillMaxSize()) {
        GoogleMap(
            cameraPositionState = cameraState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocation,
                mapStyleOptions = mapStyle,
                minZoomPreference = 10f
            ),
            uiSettings =
                MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                    compassEnabled = false,
                    indoorLevelPickerEnabled = false,
                    tiltGesturesEnabled = false
                ),
            mapColorScheme = if (isDark) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT,
            contentPadding = PaddingValues(top = topInset + 120.dp, bottom = bottomPadding + 64.dp),
            onMapClick = { viewModel.clearSelection() },
            modifier = Modifier.fillMaxSize()
        ) {
            state.atms.forEach { atm ->
                key(atm.id) {
                    val selected = atm.id == selectedId
                    val color = atm.quote.tier.color()
                    MarkerComposable(
                        atm.id,
                        color,
                        selected,
                        outline,
                        state = rememberUpdatedMarkerState(atm.atm.location.toLatLng()),
                        title = atm.atm.name,
                        zIndex = if (selected) {
                            2f
                        } else if (atm.isFree) {
                            1f
                        } else {
                            0f
                        },
                        onClick = {
                            viewModel.select(atm.id)
                            true
                        }
                    ) {
                        AtmPin(color = color, selected = selected, outline = outline)
                    }
                }
            }
        }

        // Top: search and filters
        Column(
            Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            MapSearchBar(query = state.query, onQueryChange = viewModel::setQuery)
            Spacer(Modifier.height(8.dp))
            AnimatedVisibility(visible = state.query.isNotBlank(), enter = fadeIn(), exit = fadeOut()) {
                SearchResultsCard(results = state.searchResults, onSelect = viewModel::select)
            }
            AnimatedVisibility(visible = state.query.isBlank(), enter = fadeIn(), exit = fadeOut()) {
                MapFilterChips(selected = state.filter, onSelect = viewModel::setFilter)
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = bottomPadding + 132.dp)
        )

        if (!BuildConfig.HAS_MAPS_KEY) {
            MissingMapKeyNotice(Modifier.align(Alignment.Center).padding(horizontal = 32.dp))
        }

        // Bottom: month fees, nearest free, locate me
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding)
        ) {
            MonthFeesPill(
                fees = state.feesThisMonth,
                onClick = onOpenInsights,
                modifier = Modifier.align(Alignment.BottomStart)
            )
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                PillButton(
                    text = stringResource(R.string.map_nearest_free),
                    onClick = viewModel::showNearestFree,
                    leadingIcon = R.drawable.ic_near_me,
                    compact = true,
                    enabled = state.cardBank != null
                )
                MapControlButton(
                    icon = R.drawable.ic_my_location,
                    contentDescription = stringResource(R.string.map_my_location),
                    onClick = {
                        if (hasLocation) {
                            viewModel.refreshLocation(
                                focus = true
                            )
                        } else {
                            permissionLauncher.launch(LocationPermissions)
                        }
                    }
                )
            }
        }
    }

    state.selection?.let { selection ->
        AtmDetailSheet(
            selection = selection,
            cardBank = state.cardBank,
            onDismiss = viewModel::clearSelection,
            onShowAtm = viewModel::select,
            onLogWithdrawal = {
                viewModel.clearSelection()
                loggingAtmId = it
            }
        )
    }

    loggingAtmId?.let { id ->
        state.allAtms.firstOrNull { it.id == id }?.let { atm ->
            LogWithdrawalSheet(
                atm = atm,
                quoteFor = { amount -> state.calculator.quote(state.cardBank?.id, atm.atm, amount) },
                onDismiss = { loggingAtmId = null },
                onConfirm = { amount ->
                    viewModel.log(atm.id, amount)
                    loggingAtmId = null
                }
            )
        }
    }
}

private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)
