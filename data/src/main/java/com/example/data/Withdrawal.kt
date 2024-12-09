package com.example.data

import java.time.LocalDateTime

data class Withdrawal(
    val atm: ATM,             // ATM where the withdrawal occurred
    val amount: Double,       // Amount withdrawn
    val fee: Double,          // Fee for this transaction
    val timestamp: LocalDateTime       // Time when the withdrawal occurred
)