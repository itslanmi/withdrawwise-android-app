package io.github.itslanmi.withdrawwise.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.itslanmi.withdrawwise.data.LocationProvider
import io.github.itslanmi.withdrawwise.domain.FeeTier
import io.github.itslanmi.withdrawwise.domain.model.Atm
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.model.GeoPoint
import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.domain.model.sumOfMoney
import io.github.itslanmi.withdrawwise.domain.repository.DirectoryRepository
import io.github.itslanmi.withdrawwise.domain.repository.PreferencesRepository
import io.github.itslanmi.withdrawwise.domain.repository.WithdrawalRepository
import io.github.itslanmi.withdrawwise.ui.model.AtmUi
import io.github.itslanmi.withdrawwise.ui.model.CityRadiusMeters
import io.github.itslanmi.withdrawwise.ui.model.LjubljanaCenter
import io.github.itslanmi.withdrawwise.ui.model.buildAtmUis
import java.time.Clock
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AtmFilter { All, Free, LowFee, Open24h }

data class AtmSelection(
    val atm: AtmUi,
    /** Nearest ATM that would be free for the user, shown as a tip when [atm] costs money. */
    val freeAlternative: AtmUi?,
    val freeAlternativeMeters: Double?
)

data class MapUiState(
    val loading: Boolean = true,
    val atms: List<AtmUi> = emptyList(),
    val filter: AtmFilter = AtmFilter.All,
    val query: String = "",
    val searchResults: List<AtmUi> = emptyList(),
    val selection: AtmSelection? = null,
    val cardBank: Bank? = null,
    val feesThisMonth: Money = Money.Zero,
    val userLocation: GeoPoint? = null
) {
    val isInCity: Boolean get() = userLocation?.let { it.distanceTo(LjubljanaCenter) <= CityRadiusMeters } == true
}

sealed interface MapEvent {
    data class FocusOn(val point: GeoPoint, val zoom: Float) : MapEvent
}

private data class Directory(val banks: List<Bank>, val atms: List<Atm>)

private data class LocalState(
    val filter: AtmFilter,
    val query: String,
    val selectedId: String?,
    val location: GeoPoint?
)

@HiltViewModel
class MapViewModel
@Inject
constructor(
    directory: DirectoryRepository,
    preferences: PreferencesRepository,
    withdrawals: WithdrawalRepository,
    private val locationProvider: LocationProvider,
    private val clock: Clock
) : ViewModel() {
    private val filter = MutableStateFlow(AtmFilter.All)
    private val query = MutableStateFlow("")
    private val selectedId = MutableStateFlow<String?>(null)
    private val location = MutableStateFlow<GeoPoint?>(null)
    private var hasFocusedOnUser = false

    private val events = Channel<MapEvent>(Channel.BUFFERED)
    val mapEvents: Flow<MapEvent> = events.receiveAsFlow()

    private val directoryFlow = flow { emit(Directory(directory.banks(), directory.atms())) }

    private val feesThisMonth =
        withdrawals.observeAll().map { list ->
            val zone = clock.zone ?: ZoneId.systemDefault()
            val month = YearMonth.now(clock)
            list
                .filter { YearMonth.from(it.timestamp.atZone(zone)) == month }
                .sumOfMoney { it.fee }
        }

    private val localState = combine(filter, query, selectedId, location, ::LocalState)

    /** Every ATM with fees for the current card, before filtering. Shared by state and actions. */
    private val allAtms: StateFlow<List<AtmUi>> =
        combine(directoryFlow, preferences.preferences, location) { dir, prefs, loc ->
            buildAtmUis(
                dir.atms,
                dir.banks,
                prefs.bankId,
                loc?.takeIf {
                    it.distanceTo(LjubljanaCenter) <=
                        CityRadiusMeters
                }
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val state: StateFlow<MapUiState> =
        combine(directoryFlow, preferences.preferences, allAtms, feesThisMonth, localState) {
                dir,
                prefs,
                all,
                fees,
                local
            ->
            val visible =
                all.filter {
                    when (local.filter) {
                        AtmFilter.All -> true
                        AtmFilter.Free -> it.quote.tier == FeeTier.Free
                        AtmFilter.LowFee -> it.quote.tier != FeeTier.High
                        AtmFilter.Open24h -> it.atm.isOpen24h
                    }
                }
            val results =
                if (local.query.isBlank()) {
                    emptyList()
                } else {
                    all.filter {
                        it.matches(local.query)
                    }.sortedWith(compareBy({ it.distanceMeters ?: 0.0 }, { it.atm.name })).take(MaxSearchResults)
                }
            MapUiState(
                loading = all.isEmpty(),
                atms = visible,
                filter = local.filter,
                query = local.query,
                searchResults = results,
                selection = local.selectedId?.let { id ->
                    all.firstOrNull { it.id == id }
                }?.let { selection(it, all) },
                cardBank = dir.banks.firstOrNull { it.id == prefs.bankId },
                feesThisMonth = fees,
                userLocation = local.location
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapUiState())

    fun setFilter(value: AtmFilter) {
        filter.value = value
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun select(id: String) {
        selectedId.value = id
        query.value = ""
        allAtms.value.firstOrNull {
            it.id == id
        }?.let { events.trySend(MapEvent.FocusOn(it.atm.location, FocusZoom)) }
    }

    fun clearSelection() {
        selectedId.value = null
    }

    /** Jumps to the closest ATM that is free with the user's card. */
    fun showNearestFree() {
        val reference = referencePoint(location.value)
        allAtms.value
            .filter { it.isFree }
            .minByOrNull { it.atm.location.distanceTo(reference) }
            ?.let { select(it.id) }
    }

    fun refreshLocation(focus: Boolean = false) {
        viewModelScope.launch {
            val current = locationProvider.currentLocation() ?: return@launch
            location.value = current
            val inCity = current.distanceTo(LjubljanaCenter) <= CityRadiusMeters
            if (inCity && (focus || !hasFocusedOnUser)) {
                hasFocusedOnUser = true
                events.send(MapEvent.FocusOn(current, UserZoom))
            } else if (focus) {
                events.send(MapEvent.FocusOn(LjubljanaCenter, CityZoom))
            }
        }
    }

    private fun selection(atm: AtmUi, all: List<AtmUi>): AtmSelection {
        if (atm.isFree) return AtmSelection(atm, null, null)
        val alternative =
            all
                .filter { it.isFree && it.id != atm.id }
                .minByOrNull { it.atm.location.distanceTo(atm.atm.location) }
        return AtmSelection(atm, alternative, alternative?.atm?.location?.distanceTo(atm.atm.location))
    }

    /** Distances are measured from the user when they are in Ljubljana, otherwise from the centre. */
    private fun referencePoint(location: GeoPoint?): GeoPoint =
        location?.takeIf { it.distanceTo(LjubljanaCenter) <= CityRadiusMeters } ?: LjubljanaCenter

    companion object {
        const val CityZoom = 13.6f
        const val UserZoom = 15.5f
        const val FocusZoom = 16f
        private const val MaxSearchResults = 6
    }
}
