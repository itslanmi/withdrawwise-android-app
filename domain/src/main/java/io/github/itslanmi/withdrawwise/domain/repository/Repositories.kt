package io.github.itslanmi.withdrawwise.domain.repository

import io.github.itslanmi.withdrawwise.domain.model.Atm
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.model.ThemeMode
import io.github.itslanmi.withdrawwise.domain.model.UserPreferences
import io.github.itslanmi.withdrawwise.domain.model.Withdrawal
import kotlinx.coroutines.flow.Flow

/** Read-only reference data: banks and the ATMs on the map. */
interface DirectoryRepository {
    suspend fun banks(): List<Bank>

    suspend fun atms(): List<Atm>
}

interface WithdrawalRepository {
    fun observeAll(): Flow<List<Withdrawal>>

    suspend fun get(id: Long): Withdrawal?

    suspend fun add(withdrawal: Withdrawal): Long

    suspend fun update(withdrawal: Withdrawal)

    suspend fun delete(id: Long)

    suspend fun deleteAll()
}

interface PreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun completeOnboarding(displayName: String, bankId: String)

    suspend fun setDisplayName(name: String)

    suspend fun setBank(bankId: String)

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setWeeklySummary(enabled: Boolean)

    suspend fun setMonthlySummary(enabled: Boolean)

    suspend fun reset()
}
