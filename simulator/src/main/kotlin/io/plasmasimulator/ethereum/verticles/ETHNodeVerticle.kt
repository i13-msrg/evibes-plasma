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
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.util.*


class ETHNodeVerticle : ETHBaseNode() {
  val plasmaContract = PlasmaContract()
  var accountsMap = mutableMapOf<String, Account>()
  var transactions = mutableListOf<ETHTransaction>()
  var peers = mutableListOf<String>()
  private var blockGasLimit = 0
  private var txPoolGas = 0
  var tokensPerClient = 0


  private companion object {
    private val LOG = LoggerFactory.getLogger(ETHNodeVerticle::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    startConsumers()
    tokensPerClient = config().getInteger("tokensPerClient")
    blockGasLimit = config().getInteger("blockGasLimit")
  }

  fun processContractTransaction(tx: ETHTransaction) {
    if(tx.data == null) return
    when(tx.data!!.get("method")) {
      "submitBlock" -> plasmaContract.submitBlock(tx.data!!.get("rootHash")!!)
      "deposit" -> {
        var result: JsonObject = plasmaContract.deposit(tx.data!!.get("address")!!,
                                                        tx.data!!.get("amount")!!.toInt(),
                                                        tx.data!!.get("chainAddress")!!)
        // publish deposit block to operator
        // TODO: consider sending the block to all plasma participants
        vertx.eventBus().send("${tx.data!!.get("chainAddress")!!}/${Address.ETH_ANNOUNCE_DEPOSIT.name}", result)
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

  fun startConsumers() {
    vertx.eventBus().consumer<Any>(ethAddress) { msg ->
      val jsonObject = msg.body() as JsonObject
      when(jsonObject.getString("type")) {

        "propagateTransaction" -> {
          //LOG.info("[ETH $ethAddress received propagateTransaction]")
          val txJson = jsonObject.getJsonObject("transaction")
          val tx: ETHTransaction = Json.decodeValue(txJson.toString(), ETHTransaction::class.java)

          if(!txPool.contains(tx)) {
            if(!validateTransaction(tx)) {
              LOG.info("Transaction $tx is invalid")
            } else {
              txPool.push(tx)
              txPoolGas += tx.gasLimit
              // propagate transaction to other peers
              propagateTransaction(tx)

              if(gasLimitForBlockReached()) {
                if(Random().nextInt(18) % 3 == 0) {
                  requestMining(createBlock())
                }
              }
            }
          }
          else {
            LOG.info("[$ethAddress] Transaction already there")
          }
        }

        "propagateBlock" -> {
          //LOG.info("[ETH $ethAddress received propagateBlock]")
          val blockJson = jsonObject.getJsonObject("block")
          val block: ETHBlock = Json.decodeValue(blockJson.toString(), ETHBlock::class.java)
          if(ethChain.containsBlock(block.number)) {
            LOG.info("[$ethAddress]: attempted to add block ${block.number}, but it already exists!")
          } else {
            processBlock(block)
            removeTransactionsFor(block)
            LOG.info("[$ethAddress]: added block ${block.number}")
            // propagate block to other peers
            propagateBlock(block)
          }
        }

        "setNewPeers" -> {
          //LOG.info("[ETH $ethAddress received setNewPeers]")
          peers.clear()
          jsonObject.getJsonArray("peers").forEach { peer ->
            peers.add(peer.toString())
          }
        }
      }

    }
  }

  fun removeTransactionsFor(block: ETHBlock) {
    block.transactions.forEach { tx ->
      txPool.remove(tx)
    }
  }

  fun gasLimitForBlockReached() : Boolean {
    LOG.info("[$ethAddress] txPoolGas: $txPoolGas")
    LOG.info("[$ethAddress] blockGasLimit: $blockGasLimit")
    return txPoolGas >= blockGasLimit
  }

  fun requestMining(block: ETHBlock) {
    vertx.eventBus().send<Any>(Address.READY_TO_MINE.name, JsonObject(Json.encode(block))) { response ->
      val result = response.result().body() as String
      LOG.info(result)
      if(result == Message.SUCCESS.name) {
        LOG.info("[$ethAddress] is mining a block <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>")
        mineBlock(block)
        removeTransactionsFor(block)
        println(Json.encode(block).toString())
        propagateBlock(block)
      }
    }
  }

  fun createBlock() : ETHBlock {
    var gasLimit = 0
    val txList = mutableListOf<ETHTransaction>()

    while (gasLimit < blockGasLimit) {
      val tx: ETHTransaction = txPool.pop()
      txPoolGas -= tx.gasLimit
      txList.add(tx)
      gasLimit += tx.gasLimit
    }
    val lastBlock = ethChain.getLastBlock()!!
    return ETHBlock(number = lastBlock.number + 1, prevBlockNum = lastBlock.number, transactions = txList)
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

  fun propagateTransaction(tx: ETHTransaction) {
    val data = JsonObject()
      .put("type", "propagateTransaction")
      .put("transaction", JsonObject(Json.encode(tx)))

    sendToPeers(data)
  }

  fun propagateBlock(block: ETHBlock) {
    val data = JsonObject()
      .put("type", "propagateBlock")
      .put("block", JsonObject(Json.encode(block)))

    sendToPeers(data)
  }

  fun sendToPeers(data: JsonObject) {
    for( peer in peers) {
      vertx.eventBus().send(peer, data)
    }
  }
}
