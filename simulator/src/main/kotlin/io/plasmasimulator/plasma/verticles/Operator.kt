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
  var childChains = mutableListOf<String>()
  var TRANSACTIONS_PER_BLOCK = 0

  private companion object {
    private val LOG = LoggerFactory.getLogger(Operator::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("Hello from Operator $address")

    TRANSACTIONS_PER_BLOCK = config().getInteger("transactionsPerBlock")

    if(config().containsKey("childrenPlasmaChainAddresses")) {
      childChains = config().getJsonArray("childrenPlasmaChainAddresses").list.toMutableList() as MutableList<String>
    }

    vertx.eventBus().consumer<Any>("${chain.chainAddress}/${Address.PUBLISH_TRANSACTION.name}") { msg ->
      val newTransaction = Json.decodeValue(msg.body().toString(), Transaction::class.java)
      if(newTransaction.childChainTransaction) {
        LOG.info("[$address] Child Chain Transaction received <<<<<<<<<")
        println(newTransaction.childChainData)
      }
      if(chain.validateTransaction(newTransaction, plasmaPool))
        transactions.add(newTransaction)
      else LOG.info("transaction is invalid, my friend")

      if(transactions.size >= TRANSACTIONS_PER_BLOCK) {
        val newBlock = createBlock(transactions.take(TRANSACTIONS_PER_BLOCK))
        if(applyBlock(newBlock))
          transactions = transactions.drop(TRANSACTIONS_PER_BLOCK).toMutableList()
      }
    }

    vertx.eventBus().consumer<Any>("${chain.chainAddress}/${Address.DEPOSIT_TRANSACTION.name}") { msg ->
      val depositTransaction = Json.decodeValue(msg.body().toString(), Transaction::class.java)
      applyBlock(createBlock(mutableListOf(depositTransaction)))
    }

    vertx.eventBus().consumer<Any>("${chain.chainAddress}/${Address.ETH_ANNOUNCE_DEPOSIT.name}") { msg ->
      val jsonObj = msg.body() as JsonObject

      if(!chain.containsBlock(jsonObj.getInteger("blockNum"))) {
        val tx = Transaction()
        tx.depositTransaction = true
        tx.addOutput(jsonObj.getString("address"), jsonObj.getInteger("amount"))
        var blockNumber = jsonObj.getInteger("blockNum")
        LOG.info("[$address] Operator received deposit ${jsonObj.getInteger("amount")} for ${jsonObj.getString("address")} ")
        LOG.info("Operator received block $blockNumber")
        val newBlock = createBlock(listOf(tx), blockNumber)
        //TODO: verify new block root hash is same as the once coming from contract
        applyBlock(newBlock, true)
      }
    }
  }

  fun createBlock(newTransactions: List<Transaction>, num: Int = -1) : PlasmaBlock{
      val prevBlock = chain.getLastBlock()!!
      val newBlock = PlasmaBlock(number = prevBlock.number + 1,
                                 prevBlockNum = if(num > 0)  num else prevBlock.number,
                                 prevBlockHash = prevBlock.blockHash(),
                                 transactions = newTransactions)
      if(newTransactions.size == 1) { // deposit transaction block
        val depositTxOutput = newTransactions[0].outputs[0]
        newBlock.merkleRoot = HashUtils.hash(depositTxOutput.address.toByteArray() + depositTxOutput.amount.toByte())
        return newBlock
      }

      val blockRoot = MerkleTreeBuilder.getRoot(newBlock.transactions.toMutableList())
      newBlock.merkleRoot = blockRoot.digest

      return newBlock
  }

  fun applyBlock(block: PlasmaBlock, depositBlock: Boolean = false) : Boolean {
    if(!chain.validateBlock(block, plasmaPool))
      return false

    chain.addBlock(block, plasmaPool)
    if(!depositBlock) {
      // deposit blocks come from plasma contract when a client deposits tokens
      // into the plasma chain, hence such blocks should not be submitted back
      // to the contract
      rootChainService?.submitBlock(from = address, rootHash = block.merkleRoot)
    }
    FileManager.writeNewFile(vertx, Json.encode(chain.blocks), "blockchain.json")

    removeUTXOsForBlock(block)
    createUTXOsForBlock(block)
    LOG.info("[$address] BLOCK ADDED TO BLOCKCHAIN. NUMBER OF BLOCKS: ${chain.blocks.size}")
    LOG.info("[$address] TOTAL SUM OF UTXOs: ${calculateTotalBalance()}")
    val blockJson  = JsonObject(Json.encode(block))
    send("${chain.chainAddress}/${Address.PUBLISH_BLOCK.name}", blockJson)

    if(chain.parentChainAddress != null) {
      LOG.info("[$address] Create transaction to parent")
      // send the block to the parent chain as a transaction
      val tx = Transaction()
      tx.source = address
      tx.childChainTransaction = true
      tx.childChainData.put("blockNum", block.number.toString())
      tx.childChainData.put("merkleRoot", HashUtils.transform(block.merkleRoot))
      send("${chain.parentChainAddress}/${Address.PUBLISH_TRANSACTION.name}", JsonObject(Json.encode(tx)))
    }
    // send the block to all child chains since the block could contain a transaction
    // that came from a child chain
    if(childChains.size > 0) {
      childChains.forEach{ chain ->
        send(chain, blockJson)
      }
    }
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
