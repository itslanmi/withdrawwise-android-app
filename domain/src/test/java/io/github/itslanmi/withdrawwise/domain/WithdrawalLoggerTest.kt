package io.github.itslanmi.withdrawwise.domain

import io.github.itslanmi.withdrawwise.domain.model.Atm
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.model.FeeRule
import io.github.itslanmi.withdrawwise.domain.model.GeoPoint
import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.domain.model.ThemeMode
import io.github.itslanmi.withdrawwise.domain.model.UserPreferences
import io.github.itslanmi.withdrawwise.domain.model.Withdrawal
import io.github.itslanmi.withdrawwise.domain.repository.DirectoryRepository
import io.github.itslanmi.withdrawwise.domain.repository.PreferencesRepository
import io.github.itslanmi.withdrawwise.domain.repository.WithdrawalRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WithdrawalLoggerTest {
    private val home = Bank("home", "Home", "H", 0, issuesCards = true, otherAtmFee = FeeRule(flat = Money(120)))
    private val other = Bank("other", "Other", "O", 0, issuesCards = true)
    private val ownAtm = Atm("own", "Own", "", "", GeoPoint(46.0500, 14.5000), "home", true, true)
    private val otherAtm = Atm("far", "Far", "", "", GeoPoint(46.0510, 14.5000), "other", true, true)

    private val now = Instant.parse("2026-11-27T10:00:00Z")
    private val repo = FakeWithdrawals()
    private val logger =
        WithdrawalLogger(
            directory = FakeDirectory(listOf(home, other), listOf(ownAtm, otherAtm)),
            preferences = FakePreferences(UserPreferences(onboardingComplete = true, bankId = "home")),
            withdrawals = repo,
            clock = Clock.fixed(now, ZoneOffset.UTC)
        )

    @Test
    fun `free withdrawal has no fee and no alternative`() = runTest {
        val logged = logger.log("own", Money.euros(50))
        assertEquals(Money.Zero, logged.fee)
        assertNull(logged.freeAlternativeMeters)
        assertEquals(now, logged.timestamp)
    }

    @Test
    fun `paid withdrawal records the fee and the free alternative distance`() = runTest {
        val logged = logger.log("far", Money.euros(50))
        assertEquals(Money(120), logged.fee)
        assertNotNull(logged.freeAlternativeMeters)
        assertTrue(logged.freeAlternativeMeters!! in 100..120)
    }

    @Test
    fun `updating the amount keeps id and timestamp`() = runTest {
        val logged = logger.log("far", Money.euros(50))
        logger.updateAmount(logged.id, Money.euros(100))
        val updated = repo.get(logged.id)!!
        assertEquals(Money.euros(100), updated.amount)
        assertEquals(logged.timestamp, updated.timestamp)
        assertEquals(Money(120), updated.fee)
    }

    @Test
    fun `sample data covers the last six months`() = runTest {
        logger.loadSampleData(count = 20)
        val items = repo.items.value
        assertEquals(20, items.size)
        assertTrue(items.all { it.timestamp <= now && it.timestamp >= now.minusSeconds(181L * 24 * 3600) })
        assertTrue(items.any { it.fee.isZero } && items.any { it.fee.isPositive })
    }
}

private class FakeDirectory(private val banks: List<Bank>, private val atms: List<Atm>) : DirectoryRepository {
    override suspend fun banks() = banks

    override suspend fun atms() = atms
}

private class FakePreferences(initial: UserPreferences) : PreferencesRepository {
    private val state = MutableStateFlow(initial)
    override val preferences: Flow<UserPreferences> = state

    override suspend fun completeOnboarding(displayName: String, bankId: String) {
        state.value = state.value.copy(displayName = displayName, bankId = bankId, onboardingComplete = true)
    }

    override suspend fun setDisplayName(name: String) {
        state.value = state.value.copy(displayName = name)
    }

    override suspend fun setBank(bankId: String) {
        state.value = state.value.copy(bankId = bankId)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        state.value = state.value.copy(themeMode = mode)
    }

    override suspend fun setWeeklySummary(enabled: Boolean) {
        state.value = state.value.copy(weeklySummary = enabled)
    }

    override suspend fun setMonthlySummary(enabled: Boolean) {
        state.value = state.value.copy(monthlySummary = enabled)
    }

    override suspend fun reset() {
        state.value = UserPreferences()
    }
}

private class FakeWithdrawals : WithdrawalRepository {
    val items = MutableStateFlow<List<Withdrawal>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<Withdrawal>> = items

    override suspend fun get(id: Long) = items.value.firstOrNull { it.id == id }

    override suspend fun add(withdrawal: Withdrawal): Long {
        val id = nextId++
        items.value = items.value + withdrawal.copy(id = id)
        return id
    }

    override suspend fun update(withdrawal: Withdrawal) {
        items.value = items.value.map { if (it.id == withdrawal.id) withdrawal else it }
    }

    override suspend fun delete(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        items.value = emptyList()
    }
}
