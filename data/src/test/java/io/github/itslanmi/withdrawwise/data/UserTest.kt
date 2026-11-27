package io.github.itslanmi.withdrawwise.data

import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test

class UserTest {
    private val ownBank = Bank("NLB")
    private val otherBank = Bank("OTP banka")

    @Test
    fun `withdrawal at own bank ATM is free`() {
        val user = User(UUID.randomUUID(), "Test", ownBank)
        user.makeWithdrawal(ATM("Own", 0.0, 0.0, ownBank, 1.5), 50.0)

        assertEquals(0.0, user.withdrawals.single().fee, 0.0)
    }

    @Test
    fun `withdrawal at other bank ATM charges the ATM fee`() {
        val user = User(UUID.randomUUID(), "Test", ownBank)
        user.makeWithdrawal(ATM("Other", 0.0, 0.0, otherBank, 1.5), 50.0)

        assertEquals(1.5, user.totalFeesPaid(), 0.0)
    }
}
