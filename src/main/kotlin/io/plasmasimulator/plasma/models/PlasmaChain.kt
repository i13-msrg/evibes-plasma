package io.plasmasimulator.plasma.models

import io.plasmasimulator.ethereum.ETHNetworkGateway

class PlasmaChain (var blocks: MutableList<PlasmaBlock> = mutableListOf(), var plasmaPool: UTXOPool = UTXOPool()) {

  constructor(chain: PlasmaChain): this(chain.blocks.toMutableList(), UTXOPool(chain.plasmaPool))

  val ethNetworkGateway = ETHNetworkGateway

  fun addBlock(block: PlasmaBlock) {
    if(validateBlock(block)) {
      blocks.add(block)
    }
  }

  fun validateBlock(block: PlasmaBlock) : Boolean {
    if(!validateTransactions(block.transactions))
        return false
    return true
  }

  fun validateTransactions(transactions: List<Transaction>) : Boolean {
    var localPool = UTXOPool(plasmaPool)
    for(tx in transactions) {
      if(!validateTransaction(tx, localPool))
        return false
    }
    return true
  }

  fun validateTransaction(tx: Transaction, pool: UTXOPool) : Boolean {
    return true
  }

  fun getBlock(blockNum: Int) : PlasmaBlock {
    return blocks.get(blockNum)
  }

  fun getTransaction(utxo: UTXO) : Transaction {
    return blocks.get(utxo.blockNum).transactions.get(utxo.txIndex)
  }

}
