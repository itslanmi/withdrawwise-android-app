package com.example.data

import java.util.UUID

fun main() {
    val bank = Bank("Bank of America")
    val atm = ATM("ATM 1", 15.1,12.2, bank, 1.0)
    val user = User(UUID.randomUUID(),"Alice", bank)


    user.makeWithdrawal(atm, 100.0)
    println("Total fees paid: ${user.totalFeesPaid()}")
    println("Withdrawals: ${user.withdrawals}")
    println("Withdrawal timestamp: ${user.withdrawals[0].timestamp}")
}