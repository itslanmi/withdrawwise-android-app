package io.github.itslanmi.withdrawwise.domain.model

import java.time.Instant

/**
 * A withdrawal the user logged. ATM details are copied in so history stays readable even if the
 * ATM directory changes later.
 *
 * @property freeAlternativeMeters distance from the used ATM to the nearest ATM that would have
 * been free, or `null` if the withdrawal was free or no free ATM was known.
 */
data class Withdrawal(
    val id: Long = 0,
    val atmId: String,
    val atmName: String,
    val atmBankId: String,
    val amount: Money,
    val fee: Money,
    val timestamp: Instant,
    val freeAlternativeMeters: Int? = null
)

/** The user's profile and app settings. */
data class UserPreferences(
    val onboardingComplete: Boolean = false,
    val displayName: String = "",
    val bankId: String? = null,
    val themeMode: ThemeMode = ThemeMode.System,
    val weeklySummary: Boolean = true,
    val monthlySummary: Boolean = true
)

enum class ThemeMode { System, Light, Dark }
