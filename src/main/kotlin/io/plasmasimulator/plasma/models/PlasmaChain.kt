package io.plasmasimulator.plasma.models

import io.plasmasimulator.ethereum.verticles.ETHNetworkGateway

class PlasmaChain (var blocks: MutableList<PlasmaBlock> = mutableListOf()) {

  constructor(chain: PlasmaChain): this(chain.blocks.toMutableList())

  val ethNetworkGateway = ETHNetworkGateway

  fun addBlock(block: PlasmaBlock, plasmaPool: UTXOPool) {
    if(validateBlock(block, plasmaPool)) {
      blocks.add(block)
    } else
      println("block invalid")
  }

  fun validateBlock(block: PlasmaBlock, plasmaPool: UTXOPool) : Boolean {
    if(!validateTransactions(block.transactions, plasmaPool))
        return false
    return true
  }

  fun validateTransactions(transactions: List<Transaction>, plasmaPool: UTXOPool) : Boolean {
    for(tx in transactions) {
      if(!validateTransaction(tx, plasmaPool))
        return false
    }
    return true
  }

  fun validateTransaction(tx: Transaction, plasmaPool: UTXOPool) : Boolean {
    if(containsDuplicates(tx.inputs)) {
      println("Transaction contains duplicates")
    }
    for(input in tx.inputs) {
      val utxo = UTXO(input.blockNum, input.txIndex, input.outputIndex)
      if(!plasmaPool.containsUTXO(utxo)) {
        println("UTXO not in pool")
        return false
      }
    }
    return true
  }


  fun containsDuplicates(inputs: List<Transaction.Input>) : Boolean {
    var inputsAsOutputs = mutableSetOf<UTXO>()
    for(input in inputs) {
      inputsAsOutputs.add(UTXO(input.blockNum, input.txIndex, input.outputIndex))
    }
    return inputsAsOutputs.size != inputs.size
  }

  fun getBlock(blockNum: Int) : PlasmaBlock {
    return blocks.get(blockNum)
  }

  fun getLastBlock(): PlasmaBlock {
    return blocks.last()
  }

  fun getTransaction(utxo: UTXO) : Transaction {
    return blocks.get(utxo.blockNum).transactions.get(utxo.txIndex)
  }
}
