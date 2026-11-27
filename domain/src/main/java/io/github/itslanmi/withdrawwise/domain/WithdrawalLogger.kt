package io.github.itslanmi.withdrawwise.domain

import io.github.itslanmi.withdrawwise.domain.model.Atm
import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.domain.model.Withdrawal
import io.github.itslanmi.withdrawwise.domain.repository.DirectoryRepository
import io.github.itslanmi.withdrawwise.domain.repository.PreferencesRepository
import io.github.itslanmi.withdrawwise.domain.repository.WithdrawalRepository
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.flow.first

/**
 * Records withdrawals. The fee is worked out at logging time for the user's current card, and we
 * remember how far the nearest free ATM was, which powers the "avoidable fees" insight.
 */
class WithdrawalLogger
@Inject
constructor(
    private val directory: DirectoryRepository,
    private val preferences: PreferencesRepository,
    private val withdrawals: WithdrawalRepository,
    private val clock: Clock
) {
    suspend fun log(atmId: String, amount: Money): Withdrawal {
        require(amount.isPositive) { "Amount must be positive" }
        val atm = directory.atms().first { it.id == atmId }
        val withdrawal = build(atm, amount, cardBankId(), clock.instant())
        val id = withdrawals.add(withdrawal)
        return withdrawal.copy(id = id)
    }

    /** Changes the amount of an existing withdrawal and recalculates its fee. */
    suspend fun updateAmount(id: Long, amount: Money) {
        require(amount.isPositive) { "Amount must be positive" }
        val existing = withdrawals.get(id) ?: return
        val atm = directory.atms().firstOrNull { it.id == existing.atmId }
        val updated =
            if (atm == null) {
                existing.copy(amount = amount)
            } else {
                build(atm, amount, cardBankId(), existing.timestamp).copy(id = existing.id)
            }
        withdrawals.update(updated)
    }

    /** Puts back a withdrawal that was just deleted (undo). */
    suspend fun restore(withdrawal: Withdrawal): Long = withdrawals.add(withdrawal)

    /**
     * Fills the history with a realistic mix of withdrawals over the last six months, so the
     * insights screen has something to show in demos. Deterministic for a given [seed].
     */
    suspend fun loadSampleData(count: Int = 32, seed: Int = 7) {
        val random = Random(seed)
        val atms = directory.atms()
        val calculator = FeeCalculator(directory.banks())
        val cardBankId = cardBankId()
        val (free, paid) = atms.partition { calculator.isFree(cardBankId, it) }
        val amounts = listOf(20, 30, 40, 50, 50, 60, 80, 100, 100, 150, 200)
        val now = clock.instant()
        repeat(count) {
            val pool = if (free.isNotEmpty() && (paid.isEmpty() || random.nextFloat() < 0.6f)) free else paid
            val atm = pool[random.nextInt(pool.size)]
            // A random day in the last six months, at a plausible time between 08:00 and 21:59.
            val day = now.atZone(clock.zone).toLocalDate().minusDays(random.nextLong(0, 180))
            val time = LocalTime.of(random.nextInt(8, 22), random.nextInt(0, 60))
            val timestamp = minOf(day.atTime(time).atZone(clock.zone).toInstant(), now)
            val amount = Money.euros(amounts[random.nextInt(amounts.size)])
            withdrawals.add(build(atm, amount, cardBankId, timestamp))
        }
    }

    private suspend fun cardBankId(): String? = preferences.preferences.first().bankId

    private suspend fun build(atm: Atm, amount: Money, cardBankId: String?, timestamp: java.time.Instant): Withdrawal {
        val atms = directory.atms()
        val calculator = FeeCalculator(directory.banks())
        val fee = calculator.quote(cardBankId, atm, amount).total
        val alternative =
            if (fee.isPositive) {
                calculator.nearestFreeAtm(
                    cardBankId,
                    atm.location,
                    atms,
                    excludeId = atm.id
                )
            } else {
                null
            }
        return Withdrawal(
            atmId = atm.id,
            atmName = atm.name,
            atmBankId = atm.bankId,
            amount = amount,
            fee = fee,
            timestamp = timestamp,
            freeAlternativeMeters = alternative?.second?.roundToInt()
        )
    }
}
