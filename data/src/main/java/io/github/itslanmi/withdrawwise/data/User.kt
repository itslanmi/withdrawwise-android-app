package io.github.itslanmi.withdrawwise.data

import com.google.gson.Gson
import java.time.LocalDateTime
import java.util.UUID

data class User(
    val userID: UUID,
    val name: String,
    val bank: Bank,
    var withdrawals: MutableList<Withdrawal> = mutableListOf()
) {
    // Helper function to add a withdrawal
    fun makeWithdrawal(atm: ATM, amount: Double) {
        val fee = if (bank.name == atm.bank.name) 0.0 else atm.withdrawalFee
        val withdrawal = Withdrawal(atm, amount, fee, LocalDateTime.now())
        withdrawals.add(withdrawal)
    }

    // Helper function to calculate total fees paid
    fun totalFeesPaid(): Double = withdrawals.sumOf { it.fee }

    fun gsonDeserializeWithdrawals(json: String) {
        this.withdrawals = Gson().fromJson(json, Array<Withdrawal>::class.java).toMutableList()
    }

    fun gsonSerializeWithdrawals(): String = Gson().toJson(withdrawals)
}
