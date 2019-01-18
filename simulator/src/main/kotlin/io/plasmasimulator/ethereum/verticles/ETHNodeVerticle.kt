package io.plasmasimulator.ethereum.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.conf.Message
import io.plasmasimulator.ethereum.contracts.PlasmaContract
import io.plasmasimulator.ethereum.models.Account
import io.plasmasimulator.ethereum.models.ETHBlock
import io.plasmasimulator.ethereum.models.ETHChain
import io.plasmasimulator.ethereum.models.ETHTransaction
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.util.*


class ETHNodeVerticle : ETHBaseNode() {
  private var plasmaContract = PlasmaContract(10)
  var accountsMap = mutableMapOf<String, Account>()
  private var blockGasLimit = 0
  private var txPoolGas = 0
  var tokensPerClient = 0


  private companion object {
    private val LOG = LoggerFactory.getLogger(ETHNodeVerticle::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    tokensPerClient = config().getInteger("tokensPerClient")
    blockGasLimit = config().getInteger("blockGasLimit")
    val plasmaBlockInterval = config().getInteger("plasmaBlockInterval")
    plasmaContract = PlasmaContract(plasmaBlockInterval)
  }

  fun processContractTransaction(tx: ETHTransaction) {
    if(tx.data == null) return
    when(tx.data!!.get("method")) {
      "submitBlock" -> plasmaContract.submitBlock(tx.data!!.get("rootHash")!!,
                                                  tx.data!!.get("timestamp")!!.toLong())

      "deposit" -> {
        val parentPlasmaAddress: String? = tx.data!!.get("parentPlasmaAddress")
        val chainAddress: String = tx.data!!.get("chainAddress")!!
        val result: JsonObject = plasmaContract.deposit(tx.data!!.get("address")!!,
                                                        tx.data!!.get("amount")!!.toInt(),
                                                        chainAddress)
        // publish deposit block to operator
        val destination = if(parentPlasmaAddress != null) parentPlasmaAddress else chainAddress
        vertx.eventBus().send("${destination}/${Address.ETH_ANNOUNCE_DEPOSIT.name}", result)
      }
    }
  }

  fun validateTransaction(tx: ETHTransaction) : Boolean {
    if(!accountsMap.containsKey(tx.from)) {
      accountsMap.put(tx.from, Account(1, tx.from, 0))
      return true
    }
    var account = accountsMap.get(tx.from)!!
    if(tx.nonce == account.nonce + 1) {
      account.nonce ++
      return true
    }

    return false
  }

  fun issue_transaction() {
    var accountsList = accountsMap.values.shuffled()
    val sourceIndex = Random().nextInt(accountsList.size)
    var sourceAccount = accountsList.get(sourceIndex)
    var destAccount = getDestinationAccount(sourceIndex, accountsList)
    val amountToTransfer = getRandomAmount(sourceAccount.balance)

    val newTransaction = ETHTransaction(nonce = 1,
                                        from = sourceAccount.address,
                                        to = destAccount.address,
                                        amount = amountToTransfer,
                                        gasLimit = 30,
                                        gasPrice = 20,
                                        data = null)

  }

  fun getDestinationAccount(sourceIndex: Int, accountsList: List<Account>) : Account {
    var destIndex: Int
    do {
      destIndex = Random().nextInt(accountsList.size)
    } while (destIndex == sourceIndex)
    return accountsList.get(destIndex)
  }

  fun getRandomAmount(amount: Int): Int {
    return Random().nextInt(amount)
  }

  override fun handlePropagateTransaction(tx: ETHTransaction) {
    if(!txPool.contains(tx)) {
      if(!validateTransaction(tx)) {
        //LOG.info("Transaction $tx is invalid")
      } else {
        txPool.add(tx)

        // propagate transaction to other peers
        propagateTransaction(tx)

        if(gasLimitForBlockReached()) {
          if(Random().nextInt(18) % 3 == 0) {
            requestMining(createBlock())
          }
        }
      }
    }
  }

  override fun handlePropagateTransactions(txs: List<ETHTransaction>) {
    txs.forEach { tx ->
      if(!txPool.contains(tx)) {
        if(validateTransaction(tx)) {
          txPool.add(tx)

          if(gasLimitForBlockReached()) {
            if(Random().nextInt(18) % 3 == 0) {
              requestMining(createBlock())
            }
          }
        }
      }
    }
  }

  override fun handlePropagateBlock(block: ETHBlock) {
    if(!ethChain.containsBlock(block.number)) {
      processBlock(block)
      removeTransactionsFor(block)
      // LOG.info("[$ethAddress]: added block ${block.number}")
      // propagate block to other peers
      propagateBlock(block)
      sendReceived(block.number)
    }
  }

  fun removeTransactionsFor(block: ETHBlock) {
    block.transactions.forEach { tx ->
      txPool.remove(tx)
    }
  }
  // send to ETHManager in order to determine propagation delay of blocks
  fun sendReceived(blockNumber: Int) {
    val data = JsonObject().put("blockNumber", blockNumber).put("timestamp", System.currentTimeMillis())
    vertx.eventBus().send(Address.ETH_BLOCK_RECEIVED.name, data)
  }

  fun gasLimitForBlockReached() : Boolean {
    var gas = txPoolGas()
//    LOG.info("[$ethAddress] txPoolGas: $gas")
//    LOG.info("[$ethAddress] blockGasLimit: $blockGasLimit")

    return gas >= blockGasLimit
  }

  fun txPoolGas() : Int{
    var gas = 0
    txPool.forEach { tx ->
      gas += tx.gasLimit
    }
    return gas
  }

  fun requestMining(block: ETHBlock) {
    //LOG.info("[$ethAddress] is requesting to mine block ${block.number}")
    vertx.eventBus().send<Any>(Address.READY_TO_MINE.name, JsonObject(Json.encode(block))) { response ->
      val result = response.result().body() as String
      if(result == Message.SUCCESS.name) {
        LOG.info("[$ethAddress] is mining a block ${block.number}")
        mineBlock(block)
        removeTransactionsFor(block)
        propagateBlock(block)
        sendReceived(block.number)
      }
    }
  }

  fun createBlock() : ETHBlock {
    var gasLimit = 0
    val txList = mutableListOf<ETHTransaction>()

    // copy the txPool and remove elements from the copy
    // as this block can eventually become an orphan block
    // if that happens all removed transactions from txPool will be gone
    // the copy prevents that
    var currentTxPool = txPool.toMutableList()

    while (gasLimit < blockGasLimit) {
      val tx: ETHTransaction = currentTxPool.removeAt(0)
      txList.add(tx)
      gasLimit += tx.gasLimit
    }
    val lastBlock = ethChain.getLastBlock()!!
    return ETHBlock(number = lastBlock.number + 1, prevBlockNum = lastBlock.number, transactions = txList, extraData = ethAddress)
  }

  fun processBlock(block: ETHBlock) {
    for(tx in block.transactions) {
      executeTransaction(tx)
    }
    ethChain.addBlock(block)
  }

  fun mineBlock(block: ETHBlock) {
    processBlock(block)
  }

  fun executeTransaction(tx: ETHTransaction) {
    // so far only process plasmaContractTransactions
    if(tx.data != null) {
      processContractTransaction(tx)
    }
  }

}
