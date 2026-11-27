package io.github.itslanmi.withdrawwise.data

import java.time.LocalDateTime

data class Withdrawal(val atm: ATM, val amount: Double, val fee: Double, val timestamp: LocalDateTime)
