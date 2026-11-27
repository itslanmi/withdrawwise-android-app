package io.github.itslanmi.withdrawwise.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.itslanmi.withdrawwise.R
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

@Serializable
data object MapRoute

@Serializable
data object HistoryRoute

@Serializable
data object InsightsRoute

@Serializable
data object ProfileRoute

/** The four tabs of the floating navigation bar. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
    @param:DrawableRes val selectedIcon: Int
) {
    Map(MapRoute, MapRoute::class, R.string.nav_map, R.drawable.ic_map, R.drawable.ic_map_filled),
    History(
        HistoryRoute,
        HistoryRoute::class,
        R.string.nav_history,
        R.drawable.ic_receipt_long,
        R.drawable.ic_receipt_long_filled
    ),
    Insights(
        InsightsRoute,
        InsightsRoute::class,
        R.string.nav_insights,
        R.drawable.ic_bar_chart,
        R.drawable.ic_bar_chart_filled
    ),
    Profile(ProfileRoute, ProfileRoute::class, R.string.nav_profile, R.drawable.ic_person, R.drawable.ic_person_filled)
}
