package io.plasmasimulator.ethereum.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.ethereum.contracts.PlasmaContract
import io.plasmasimulator.ethereum.models.Account
import io.plasmasimulator.ethereum.models.ETHChain
import io.plasmasimulator.ethereum.models.ETHTransaction
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.util.*


class ETHNodeVerticle : AbstractVerticle() {
  val plasmaContract = PlasmaContract()
  var accountsMap = mutableMapOf<String, Account>()
  var localChain = ETHChain()
  var transactions = mutableListOf<ETHTransaction>()
  val TRANSACTIONS_PER_BLOCK = 10


  private companion object {
    private val LOG = LoggerFactory.getLogger(ETHNodeVerticle::class.java)
  }

  override fun start(startFuture: Future<Void>) {
    vertx.eventBus().consumer<Any>(Address.ETH_NODES_BROADCAST.name) { msg ->
      val jsonObject= msg.body() as JsonObject
      LOG.info("RECEIVED MESSAGE")

//      when(configJSON.getString("type")) {
//        Message.ISSUE_TRANSACTION.name -> issue_transaction()
//      }
    }

    vertx.eventBus().consumer<Any>(Address.APPLY_BLOCK.name) { msg ->
      val jsonObject= msg.body() as JsonObject
      LOG.info("RECEIVED MESSAGE")

//      when(configJSON.getString("type")) {
//        Message.ISSUE_TRANSACTION.name -> issue_transaction()
//      }
    }

    vertx.eventBus().consumer<Any>(Address.ETH_SUBMIT_TRANSACTION.name) { msg ->
      val tx: ETHTransaction = Json.decodeValue(msg.body().toString(), ETHTransaction::class.java)
      if(tx.data != null) {
        processContractTransaction(tx)
      }
      transactions.add(tx)
    }

    println("Hello from ETHNodeVerticle")
    startFuture.complete()
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

  fun issue_transaction() {
    var accountsList = accountsMap.values.shuffled()
    val sourceIndex = Random().nextInt(accountsList.size)
    var sourceAccount = accountsList.get(sourceIndex)
    var destAccount = getDestinationAccount(sourceIndex, accountsList)
    val amountToTransfer = getRandomAmount(sourceAccount.balance)

    val newTransaction = ETHTransaction(nonce = 1, from = sourceAccount.address, to = destAccount.address, amount = amountToTransfer, data = null)

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
}
