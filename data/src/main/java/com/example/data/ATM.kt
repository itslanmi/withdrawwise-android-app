package com.example.data

data class ATM(
    val name: String,         // ATM name or location
    val location: String,     // Location description
    val bank: Bank,           // Bank that owns this ATM
    val withdrawalFee: Double // Fee for using the ATM (if different bank)
)
