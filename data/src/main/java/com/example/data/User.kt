package com.example.data

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val userID: UUID,             // User's ID
    val name: String,             // User's name
    val bank: Bank,               // User's bank
    val withdrawals: MutableList<Withdrawal> = mutableListOf() // List of previous withdrawals
) {
    // Helper function to add a withdrawal
    fun makeWithdrawal(atm: ATM, amount: Double) {
        val fee = if (bank.name == atm.bank.name) 0.0 else atm.withdrawalFee
        val withdrawal = Withdrawal(atm, amount, fee, LocalDateTime.now())
        withdrawals.add(withdrawal)
    }

    // Helper function to calculate total fees paid
    fun totalFeesPaid(): Double {
        return withdrawals.sumOf { it.fee }
    }
}