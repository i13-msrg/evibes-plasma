package io.plasmasimulator.plasma.models

import io.plasmasimulator.ethereum.verticles.ETHNetworkGateway
import io.plasmasimulator.utils.FileManager
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import org.slf4j.LoggerFactory
import java.util.ArrayList



class PlasmaChain (var blocks: MutableList<PlasmaBlock> = mutableListOf()) {
  var vertx: Vertx? = null
  constructor(chain: PlasmaChain): this(chain.blocks.toMutableList())

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaChain::class.java)

  }

  fun addBlock(block: PlasmaBlock, plasmaPool: UTXOPool) {
    if(validateBlock(block, plasmaPool)) {
      blocks.add(block)
    } else
      LOG.info("block invalid")
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
    if(!areValuesCorrect(tx, plasmaPool) and !tx.depositTransaction) {
      LOG.info("Transaction inputs sum more than outputs")
      LOG.info(Json.encode(tx))
    }
    if(containsDuplicates(tx.inputs)) {
      LOG.info("Transaction contains duplicates")
    }
    for(input in tx.inputs) {
      val utxo = UTXO(input.blockNum, input.txIndex, input.outputIndex)
      if(!plasmaPool.containsUTXO(utxo)) {
        LOG.info("UTXO not in pool")
        if(vertx != null) {
          //FileManager.writeFile(vertx!!, Json.encode(plasmaPool.poolMap.keys), "plasmaPoolCHAIN.json")
        }
        LOG.info(Json.encode(utxo).toString())
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

  private fun areValuesCorrect(tx: Transaction, pool: UTXOPool): Boolean {
    val txOutputs = tx.outputs
    var txOutputsSum: Int = 0

    for (output in txOutputs) {
      if (output.amount < 0) return false
      txOutputsSum += output.amount
    }
    val txInputsSum = getTxInputSum(tx.inputs, pool)

    return txInputsSum >= txOutputsSum
  }

  private fun getTxOutputSum(outputs: List<Transaction.Output>): Int {
    var txOutputsSum = 0
    for (output in outputs) {
      txOutputsSum += output.amount
    }
    return txOutputsSum
  }

  private fun getTxInputSum(inputs: List<Transaction.Input>, pool: UTXOPool): Int {
    var txOutputsSum = 0

    for(input in inputs) {
      val prevUTXO = UTXO(input.blockNum, input.txIndex, input.outputIndex)
      val prevOutput = pool.getTxOutput(prevUTXO)
      if(prevOutput != null)
        txOutputsSum += prevOutput.amount
    }
    return txOutputsSum
  }
}
