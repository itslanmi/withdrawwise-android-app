package io.github.itslanmi.withdrawwise.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.itslanmi.withdrawwise.domain.model.ThemeMode
import io.github.itslanmi.withdrawwise.domain.model.UserPreferences
import io.github.itslanmi.withdrawwise.domain.repository.PreferencesRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class DataStorePreferencesRepository
@Inject
constructor(private val dataStore: DataStore<Preferences>) :
    PreferencesRepository {
    override val preferences: Flow<UserPreferences> =
        dataStore.data
            .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
            .map { prefs ->
                UserPreferences(
                    onboardingComplete = prefs[Keys.OnboardingComplete] ?: false,
                    displayName = prefs[Keys.DisplayName].orEmpty(),
                    bankId = prefs[Keys.BankId],
                    themeMode =
                        prefs[Keys.ThemeMode]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                            ?: ThemeMode.System,
                    weeklySummary = prefs[Keys.WeeklySummary] ?: true,
                    monthlySummary = prefs[Keys.MonthlySummary] ?: true
                )
            }

    override suspend fun completeOnboarding(displayName: String, bankId: String) {
        dataStore.edit {
            it[Keys.DisplayName] = displayName.trim()
            it[Keys.BankId] = bankId
            it[Keys.OnboardingComplete] = true
        }
    }

    override suspend fun setDisplayName(name: String) {
        dataStore.edit { it[Keys.DisplayName] = name.trim() }
    }

    override suspend fun setBank(bankId: String) {
        dataStore.edit { it[Keys.BankId] = bankId }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.ThemeMode] = mode.name }
    }

    override suspend fun setWeeklySummary(enabled: Boolean) {
        dataStore.edit { it[Keys.WeeklySummary] = enabled }
    }

    override suspend fun setMonthlySummary(enabled: Boolean) {
        dataStore.edit { it[Keys.MonthlySummary] = enabled }
    }

    override suspend fun reset() {
        dataStore.edit { it.clear() }
    }

    private object Keys {
        val OnboardingComplete = booleanPreferencesKey("onboarding_complete")
        val DisplayName = stringPreferencesKey("display_name")
        val BankId = stringPreferencesKey("bank_id")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val WeeklySummary = booleanPreferencesKey("weekly_summary")
        val MonthlySummary = booleanPreferencesKey("monthly_summary")
    }
}
