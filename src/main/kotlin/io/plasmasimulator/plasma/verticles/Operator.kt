package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasma.models.PlasmaChain
import io.plasmasimulator.plasma.models.Transaction
import io.plasmasimulator.utils.MerkleTreeBuilder
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
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
      if(transactions.size == TRANSACTIONS_PER_BLOCK)
        createBlock(transactions)
      val newBlock: PlasmaBlock = Json.decodeValue(msg.body().toString(), PlasmaBlock::class.java)
      applyBlock(newBlock)
    }
  }

  fun createBlock(transactions: List<Transaction>) : PlasmaBlock{
      val prevBlock = chain.getLastBlock()
      val newBlock = PlasmaBlock(number = prevBlock.number + 1,
                                 prevBlockNum = prevBlock.number,
                                 prevBlockHash = prevBlock.hash.toByteArray(),
                                 transactions = transactions)
      val blockRoot = MerkleTreeBuilder.getRoot(newBlock.transactions)
      newBlock.merkleRoot = blockRoot.digest.toMutableList()

      return newBlock


  }

  fun applyBlock(block: PlasmaBlock) : Boolean {
    if(!chain.validateBlock(block)) return false
    send(Address.APPLY_BLOCK.name, block.merkleRoot)
    return true
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }

  fun send(address: String, msg: Any?) {
    vertx.eventBus().publish(address,
      msg)
  }

}
