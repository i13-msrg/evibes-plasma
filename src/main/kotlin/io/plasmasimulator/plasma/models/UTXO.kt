package io.plasmasimulator.plasma.models

class UTXO(val blockNum: Int, val txIndex: Int, val index: Int, val fromContract: Boolean = false)
