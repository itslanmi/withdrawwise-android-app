package com.example.withdrawwise

import android.app.Application
import android.util.Log
import com.example.data.ATM
import com.example.data.Bank
import com.example.data.User
import com.google.gson.Gson
import java.util.UUID

class MyApplication : Application() {
    private lateinit var user: User
    var ATMlist = mutableListOf<ATM>()

    override fun onCreate() {
        super.onCreate()

        user = User(
            userID = getUserId(),
            name = "Milan Momcilovic",
            bank = Bank("UniCredit Banka Slovenija")
        )

        readATMData()
        readUserWithdrawals()
//
//        val atmList = listOf(
//            ATM(name = "Nova KBM Main Square", latitude = 46.554650, longitude = 15.646881, bank = Bank("Nova KBM"), withdrawalFee = 1.5),
//            ATM(name = "NLB Lent Riverside", latitude = 46.558135, longitude = 15.647986, bank = Bank("NLB Bank"), withdrawalFee = 1.2),
//            ATM(name = "UniCredit City Center", latitude = 46.554930, longitude = 15.639203, bank = Bank("UniCredit Banka Slovenija"), withdrawalFee = 1.7),
//            ATM(name = "SKB Railway Station", latitude = 46.554164, longitude = 15.646433, bank = Bank("SKB Banka"), withdrawalFee = 1.3),
//            ATM(name = "BKS Park Avenue", latitude = 46.560389, longitude = 15.650232, bank = Bank("BKS Bank"), withdrawalFee = 1.4),
//            ATM(name = "Banka Intesa Vodnikov Square", latitude = 46.557221, longitude = 15.647384, bank = Bank("Banka Intesa Sanpaolo"), withdrawalFee = 1.5),
//            ATM(name = "Delavska Hranilnica University Square", latitude = 46.562151, longitude = 15.644718, bank = Bank("Delavska Hranilnica"), withdrawalFee = 1.1),
//            ATM(name = "Bank Sparkasse Old Town", latitude = 46.554052, longitude = 15.638452, bank = Bank("Bank Sparkasse"), withdrawalFee = 1.2),
//            ATM(name = "Gorenjska Banka Tezno Area", latitude = 46.548540, longitude = 15.653174, bank = Bank("Gorenjska Banka"), withdrawalFee = 1.3),
//            ATM(name = "Nova KBM Europark", latitude = 46.550338, longitude = 15.650928, bank = Bank("Nova KBM"), withdrawalFee = 1.7),
//            ATM(name = "NLB Tabor District", latitude = 46.563457, longitude = 15.632945, bank = Bank("NLB Bank"), withdrawalFee = 1.3),
//            ATM(name = "SKB Tržaška Road", latitude = 46.549620, longitude = 15.660847, bank = Bank("SKB Banka"), withdrawalFee = 1.2),
//            ATM(name = "BKS Pohorje Base", latitude = 46.534728, longitude = 15.622152, bank = Bank("BKS Bank"), withdrawalFee = 1.4),
//            ATM(name = "UniCredit Magdalena Center", latitude = 46.561813, longitude = 15.641435, bank = Bank("UniCredit Banka Slovenija"), withdrawalFee = 1.2),
//            ATM(name = "Bank Sparkasse Glavni Bridge", latitude = 46.554724, longitude = 15.641928, bank = Bank("Bank Sparkasse"), withdrawalFee = 1.3)
//        )
//
//        for (it in atmList) {
//            addATM(it)
//        }
//
//        // Serialize the ATMs and save to file
//        val atmJson = gsonSerializeATMs()
//        try {
//            val fileOutputStream = openFileOutput("atm_data.json", MODE_PRIVATE)
//            fileOutputStream.write(atmJson.toByteArray())
//            fileOutputStream.close()
//            Log.d("MyApplication", "ATM data saved successfully")
//        } catch (e: Exception) {
//            Log.e("MyApplication", "Error saving ATM data to file", e)
//        }
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

    fun getSelectedATM(name: String?): ATM {
        return ATMlist.find { it.name == name } ?: ATMlist[0]
    }

    fun gsonSerializeATMs(): String {
        return Gson().toJson(ATMlist)
    }

    fun gsonDeserializeATMs(json: String) {
        this.ATMlist = Gson().fromJson(json, Array<ATM>::class.java).toMutableList()
    }

    fun readUserWithdrawals(): String {
        return try {
            val fileInputStream = openFileInput("user_withdrawals.json")
            val json = fileInputStream.bufferedReader().use { it.readText() }
            user.gsonDeserializeWithdrawals(json)
            "User data loaded successfully"
        } catch (e: Exception) {
            Log.e("MyApplication", "Error reading user data from file", e)
            "Error reading user data"
        }
    }

    fun saveUserWithdrawals() {
        val json = user.gsonSerializeWithdrawals()
        try {
            openFileOutput("user_withdrawals.json", MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
        } catch (e: Exception) {
            Log.e("MyApplication", "Error saving user data to file", e)
        }
    }

    fun readATMData(): String {
        return try {
            val fileInputStream = openFileInput("atm_data.json")
            val json = fileInputStream.bufferedReader().use { it.readText() }
            gsonDeserializeATMs(json)
            "ATM data loaded successfully"
        } catch (e: Exception) {
            Log.e("MyApplication", "Error reading ATM data from file", e)
            "Error reading ATM data"
        }
    }
}