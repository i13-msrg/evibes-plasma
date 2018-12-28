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
  var transactionsPerBlock = 0
  var plasmaBlockInterval = 10
  var nextBlockNumber = 10
  var receivedDepositBlocks = mutableListOf<Int>()


  private companion object {
    private val LOG = LoggerFactory.getLogger(Operator::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("Hello from Operator $address")

    transactionsPerBlock = config().getInteger("transactionsPerBlock")
    plasmaBlockInterval = config().getInteger("plasmaBlockInterval")
    nextBlockNumber = plasmaBlockInterval

    if(config().containsKey("childrenPlasmaChainAddresses")) {
      childChains = config().getJsonArray("childrenPlasmaChainAddresses").list.toMutableList() as MutableList<String>
    }

    vertx.eventBus().consumer<Any>("${chain.chainAddress}/${Address.PUBLISH_TRANSACTION.name}") { msg ->
      val newTransaction = Json.decodeValue(msg.body().toString(), Transaction::class.java)

      // nested transaction
      if(newTransaction.childChainTransaction) {
        LOG.info("[$address] Child Chain Transaction received <<<<<<<<<")
        println(newTransaction.childChainData)
      }

      if(chain.validateTransaction(newTransaction, plasmaPool))
        transactions.add(newTransaction)
      else LOG.info("transaction is invalid, my friend")

      if(transactions.size >= transactionsPerBlock) {
        // create new block
        val newBlock = createBlock(transactions.take(transactionsPerBlock), nextBlockNumber)
        updateNextBlockNumber()
        if(applyBlock(newBlock))
          transactions = transactions.drop(transactionsPerBlock).toMutableList()
      }
    }

    vertx.eventBus().consumer<Any>("${chain.chainAddress}/${Address.ETH_ANNOUNCE_DEPOSIT.name}") { msg ->
      val data = msg.body() as JsonObject

      val blockNumber = data.getInteger("blockNum")
      val address = data.getString("address")
      val amount = data.getInteger("amount")

      if(!receivedDepositBlocks.contains(blockNumber)) {
        receivedDepositBlocks.add(blockNumber)
        val tx = createTxForDepositBlock(address, amount)

        LOG.info("[$address] Operator received deposit $amount for $address ")

        val newBlock = createBlock(listOf(tx), blockNumber)
        applyBlock(newBlock, true)
      }
    }
  }

  fun createBlock(newTransactions: List<Transaction>, number: Int = -1) : PlasmaBlock {
      val newBlock = PlasmaBlock(number = number,
                                 prevBlockNum = number - 1,
                                 transactions = newTransactions)

      if(newTransactions.size == 1 && !newTransactions[0].childChainTransaction) { // deposit transaction block
        val depositTxOutput = newTransactions[0].outputs[0]
        newBlock.merkleRoot = HashUtils.hash(depositTxOutput.address.toByteArray() + depositTxOutput.amount.toByte())
        return newBlock
      }

      val blockRoot = MerkleTreeBuilder.getRoot(newBlock.transactions.toMutableList())
      newBlock.merkleRoot = blockRoot.digest

      return newBlock
  }

  fun applyBlock(block: PlasmaBlock, depositBlock: Boolean = false) : Boolean {
    if(!chain.validateBlock(block, plasmaPool)) {
      println("invalid block")
      return false
    }

    chain.addBlock(block, plasmaPool)
    if(!depositBlock && chain.parentChainAddress == null) {
      // deposit blocks come from plasma contract when a client deposits tokens
      // into the plasma chain, hence such blocks should not be submitted back
      // to the contract
      rootChainService.submitBlock(from = address, rootHash = block.merkleRoot)
    }
    //FileManager.writeNewFile(vertx, Json.encode(chain.blocks), "blockchain.json")

    removeUTXOsForBlock(block)
    createUTXOsForBlock(block)
    LOG.info("[$address] BLOCK ADDED TO BLOCKCHAIN. LOCKS: ${chain.blocks.size}")
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

  fun updateNextBlockNumber() {
    nextBlockNumber += plasmaBlockInterval
  }

  fun createTxForDepositBlock(address: String, amount: Int) : Transaction {
    val tx = Transaction()
    tx.depositTransaction = true
    tx.addOutput(address, amount)

    return tx
  }

}
