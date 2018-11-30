package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasma.models.Transaction
import io.plasmasimulator.utils.MerkleTreeBuilder
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

class Operator: PlasmaParticipant() {
  var transactions = mutableListOf<Transaction>()
  val TRANSACTIONS_PER_BLOCK = 10

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaParticipant::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)

    vertx.eventBus().consumer<Any>(Address.PUBLISH_TRANSACTION.name) { msg ->
      val newTransaction = Json.decodeValue(msg.body().toString(), Transaction::class.java)
      // TODO: should transaction be validated before added to the block
      transactions.add(newTransaction)
      if(transactions.size == TRANSACTIONS_PER_BLOCK) {
        val newBlock = createBlock(transactions)
        println("ADDING NEW BLOCK")
        transactions.clear()
        applyBlock(newBlock)
      }
    }

    vertx.eventBus().consumer<Any>(Address.DEPOSIT_TRANSACTION.name) { msg ->
      val depositTransaction = Json.decodeValue(msg.body().toString(), Transaction::class.java)

      applyBlock(createBlock(mutableListOf(depositTransaction)))
    }
  }

  fun createBlock(transactions: List<Transaction>) : PlasmaBlock{
      val prevBlock = chain.getLastBlock()
      println("PREV BLOCK NUM = ${prevBlock.number}")
      val newBlock = PlasmaBlock(number = prevBlock.number + 1,
                                 prevBlockNum = prevBlock.number,
                                 prevBlockHash = prevBlock.blockHash().toByteArray(),
                                 transactions = transactions)

      if(transactions.size == 1) { // deposit transaction block
        newBlock.merkleRoot = transactions[0].txHashCode().toMutableList()
        return newBlock
      }
      println("NUMBER OF TXS : ${transactions.size}")
      println(transactions)
      val blockRoot = MerkleTreeBuilder.getRoot(newBlock.transactions.toMutableList())
      newBlock.merkleRoot = blockRoot.digest.toMutableList()

      return newBlock
  }

  fun applyBlock(block: PlasmaBlock) : Boolean {
    if(!chain.validateBlock(block)) return false
    chain.addBlock(block)
    removeUTXOsForBlock(block)
    createUTXOsForBlock(block)
    val blockJson  = JsonObject(Json.encode(block))
    //send(Address.APPLY_BLOCK.name, blockJson) PLASMA CONTRACT
    send(Address.PUBLISH_BLOCK.name, blockJson)
    return true
  }
  // TODO: create UTXOs from outputs
  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }

  fun send(address: String, msg: Any?) {
    vertx.eventBus().publish(address, msg)
  }

}
