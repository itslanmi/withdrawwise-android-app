package com.example.data

import com.google.gson.Gson
import java.time.LocalDateTime
import java.util.UUID

data class User(
    val userID: UUID,             // User's ID
    val name: String,             // User's name
    val bank: Bank,               // User's bank
    var withdrawals: MutableList<Withdrawal> = mutableListOf() // List of previous withdrawals
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

    fun gsonDeserializeWithdrawals(json: String){
        this.withdrawals = Gson().fromJson(json, Array<Withdrawal>::class.java).toMutableList()
    }

    fun gsonSerializeWithdrawals(): String {
        return Gson().toJson(withdrawals)
    }
}