package com.example.withdrawwise

import android.app.Application
import com.example.data.ATM
import com.example.data.Bank
import com.example.data.User
import java.util.UUID

class MyApplication : Application() {
    private lateinit var user: User
    var ATMlist = mutableListOf<ATM>()

    override fun onCreate() {
        super.onCreate()

        user = User(
            userID = getUserId(),
            name = "Milan Momcilovic",
            bank = Bank("Bank of America")
        )
    }

    fun addATM(atm: ATM) {
        ATMlist.add(atm)
    }

    fun getUser(): User {
        return user
    }

    private fun getUserId(): UUID {
        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        val userIdString = sharedPreferences.getString("user_id", null)
        return if (userIdString != null) {
            UUID.fromString(userIdString)
        } else {
            val newUserId = UUID.randomUUID()
            with(sharedPreferences.edit()) {
                putString("user_id", newUserId.toString())
                apply()
            }
            newUserId
        }
    }
}