package io.github.itslanmi.withdrawwise.data

/**
 * A cash machine on the map.
 *
 * @property name ATM name or location.
 * @property bank Bank that operates this ATM.
 * @property withdrawalFee Fee charged when the card belongs to a different bank.
 */
data class ATM(val name: String, val latitude: Double, val longitude: Double, val bank: Bank, val withdrawalFee: Double)
