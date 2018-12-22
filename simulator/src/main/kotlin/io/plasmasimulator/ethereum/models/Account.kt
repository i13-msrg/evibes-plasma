package io.plasmasimulator.ethereum.models

open class Account(var nonce: Int, val address: String, var balance: Int)

/*
* nonce -> number of transactions sent by this account (address)
* */
// open class Account(nonce: Int, val address: String, var balance: Int)
