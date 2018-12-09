package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasma.models.Transaction
import io.plasmasimulator.utils.FileManager
import io.plasmasimulator.utils.HashUtils
import io.plasmasimulator.utils.MerkleTreeBuilder
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.io.File
import jdk.nashorn.internal.objects.NativeArray.forEach
import io.vertx.core.file.AsyncFile
import io.vertx.core.file.OpenOptions



class Operator: PlasmaParticipant() {
  var transactions = mutableListOf<Transaction>()
  val TRANSACTIONS_PER_BLOCK = 3
  var counter = 0

  private companion object {
    private val LOG = LoggerFactory.getLogger(Operator::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)

    vertx.eventBus().consumer<Any>(Address.PUBLISH_TRANSACTION.name) { msg ->
      val newTransaction = Json.decodeValue(msg.body().toString(), Transaction::class.java)
      counter++
      if(chain.validateTransaction(newTransaction, plasmaPool))
        transactions.add(newTransaction)
      else LOG.info("transaction is invalid, my friend")

      if(transactions.size >= TRANSACTIONS_PER_BLOCK) {
        val newBlock = createBlock(transactions.take(TRANSACTIONS_PER_BLOCK))
        if(applyBlock(newBlock))
          transactions = transactions.drop(TRANSACTIONS_PER_BLOCK).toMutableList()
      }


//      do {
//        LOG.info("creating block")
//        // TODO: send only TRANSACTIONS_PER_BLOCK number of transactions, not everyone
//        val newBlock = createBlock(transactions.take(TRANSACTIONS_PER_BLOCK))
//        if(applyBlock(newBlock))
//          transactions = transactions.drop(TRANSACTIONS_PER_BLOCK).toMutableList()
//      } while (transactions.size >= TRANSACTIONS_PER_BLOCK)
    }

//    vertx.setPeriodic(10000) {id ->
//      if(transactions.size >= TRANSACTIONS_PER_BLOCK) {
//        val newBlock = createBlock(transactions.take(TRANSACTIONS_PER_BLOCK))
//        if (applyBlock(newBlock))
//          transactions = transactions.drop(TRANSACTIONS_PER_BLOCK).toMutableList()
//      }
//    }

    vertx.eventBus().consumer<Any>(Address.DEPOSIT_TRANSACTION.name) { msg ->
      val depositTransaction = Json.decodeValue(msg.body().toString(), Transaction::class.java)

      applyBlock(createBlock(mutableListOf(depositTransaction)))
    }

    vertx.eventBus().consumer<Any>(Address.ETH_ANNOUNCE_DEPOSIT.name) { msg ->
      val jsonObj = msg.body() as JsonObject
      val tx = Transaction()
      tx.depositTransaction = true
      tx.addOutput(jsonObj.getString("address"), jsonObj.getInteger("amount"))
      val newBlock = createBlock(listOf(tx))
      //TODO: verify new block root hash is same as the once coming from contract
      if(HashUtils.transform(newBlock.merkleRoot) == jsonObj.getString("rootHash"))
        println("PAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASSSS")
      else
        println("NO PAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASSSS")
      //applyBlock(newBlock)
    }
  }

  fun createBlock(newTransactions: List<Transaction>) : PlasmaBlock{
      val prevBlock = chain.getLastBlock()!!
      val newBlock = PlasmaBlock(number = prevBlock.number + 1,
                                 prevBlockNum = prevBlock.number,
                                 prevBlockHash = prevBlock.blockHash(),
                                 transactions = newTransactions)
      if(newTransactions.size == 1) { // deposit transaction block
        val depositTxOutput = newTransactions[0].outputs[0]
        newBlock.merkleRoot = HashUtils.hash(depositTxOutput.address.toByteArray() + depositTxOutput.address.toByte())
        return newBlock
      }

      val blockRoot = MerkleTreeBuilder.getRoot(newBlock.transactions.toMutableList())
      newBlock.merkleRoot = blockRoot.digest

      return newBlock
  }

  fun applyBlock(block: PlasmaBlock) : Boolean {
    if(!chain.validateBlock(block, plasmaPool))
      return false

    chain.addBlock(block, plasmaPool)
    rootChainService.submitBlock(from = address, rootHash = block.merkleRoot)
    FileManager.writeNewFile(vertx, Json.encode(chain.blocks), "blockchain.json")

    removeUTXOsForBlock(block)
    createUTXOsForBlock(block)
    LOG.info("BLOCK ADDED TO BLOCKCHAIN. NUMBER OF BLOCKS: ${chain.blocks.size}")
    LOG.info("TOTAL SUM OF UTXOs: ${calculateTotalBalance()}")
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

  fun calculateTotalBalance(): Int {
    var total = 0
    plasmaPool.poolMap.forEach { (utxo, output) ->
        total += output.amount
    }
    return total
  }

}
