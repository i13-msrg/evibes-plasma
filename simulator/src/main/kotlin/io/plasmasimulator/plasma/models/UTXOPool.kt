package io.plasmasimulator.plasma.models

class UTXOPool(var poolMap: MutableMap<UTXO, Transaction.Output> = mutableMapOf()) {

  constructor(pool: UTXOPool) : this(pool.poolMap.toMutableMap())

  fun addUTXO(utxo: UTXO, txOutput: Transaction.Output) {
    if(!poolMap.containsKey(utxo))
      poolMap.put(utxo, txOutput)
  }

  fun removeUTXO(utxo: UTXO) {
    if(!poolMap.containsKey(utxo)) {
      println("CANNOT REMOVE UTXO. IT DOES NOT EXIST")
    }
    poolMap.remove(utxo)
  }

  fun containsUTXO(utxo: UTXO) : Boolean {
    return poolMap.containsKey(utxo)
  }

  fun poolSize() : Int {
    return poolMap.size
  }

  fun getTxOutput(utxo: UTXO) : Transaction.Output? {
    return poolMap.get(utxo)
  }
}
