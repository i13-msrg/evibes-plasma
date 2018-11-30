package io.plasmasimulator.plasma.models

class UTXOPool(var poolMap: MutableMap<UTXO, Transaction.Output> = mutableMapOf()) {

  constructor(pool: UTXOPool) : this(pool.poolMap.toMutableMap())

  fun addUTXO(utxo: UTXO, txOutput: Transaction.Output) {
    poolMap.put(utxo, txOutput)
  }

  fun removeUTXO(utxo: UTXO) {
    poolMap.remove(utxo)
  }

  fun containsUTXO(utxo: UTXO) : Boolean {
    return poolMap.containsKey(utxo)
  }

  fun getTxOutput(utxo: UTXO) : Transaction.Output? {
    return poolMap.get(utxo)
  }
}
