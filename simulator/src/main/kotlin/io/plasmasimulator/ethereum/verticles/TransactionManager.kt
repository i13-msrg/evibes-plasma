package io.plasmasimulator.ethereum.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.ethereum.models.Account
import io.plasmasimulator.ethereum.models.ETHBlock
import io.plasmasimulator.ethereum.models.ETHTransaction
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.util.*

class TransactionManager : ETHBaseNode() {

  var externalAccounts = mutableMapOf<String, Account>()
  var gasPrice = 0
  var gasLimit = 0
  var batchNumber = 0

  companion object {
      val LOG = LoggerFactory.getLogger(TransactionManager::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("TransactionManager deployed!")

    val numberOfAccounts = config().getInteger("numberOfEthereumExternalAccounts")
    val amountPerAccount = config().getInteger("amountPerEthereumExternalAccount")
    gasLimit = config().getInteger("externalGasLimit")
    gasPrice = config().getInteger("externalGasPrice")

    generateAccounts(numberOfAccounts, amountPerAccount)

    vertx.eventBus().consumer<Any>(Address.ETH_ISSUE_TRANSACTIONS.name) { msg ->
      LOG.info("CREATING TRANSACTIONS .... ")
      val transactions = createTransactions()
//      peers.forEach { peer ->
//        transactions.forEach { tx ->
//          val data = JsonObject().put("type", "propagateTransaction")
//                                             .put("transaction", JsonObject(Json.encode(tx)))
//          vertx.eventBus().send(peer, data)
//        }
//
//      }
      val jsonTXs = JsonArray()
      transactions.forEach { tx ->
        jsonTXs.add(JsonObject(Json.encode(tx)))
      }
      val data = JsonObject().put("type", "propagateTransactions")
                                         .put("transactions", jsonTXs)
                                         .put("batchNumber", batchNumber++)
      peers.forEach { peer ->
        vertx.eventBus().send(peer, data)
      }
    }
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }

  fun generateAccounts(number: Int, amountPerAccount: Int) {
    for(i in 0 until number) {
      val address = UUID.randomUUID().toString()
      externalAccounts.put(address, Account(nonce = 0, address = address, balance = amountPerAccount))
    }
  }

  fun createTransactions() : List<ETHTransaction> {
    var transactions = mutableListOf<ETHTransaction>()

    externalAccounts.values.forEach { account ->
      transactions.add(createTransaction(account))
    }

    return transactions
  }

  fun createTransaction(account: Account) : ETHTransaction {
    var otherAccounts = externalAccounts.values.filter { acc -> acc.address != account.address  }
    var shuffledAccounts = otherAccounts.shuffled()
    var destAccount = shuffledAccounts.get(Random().nextInt(shuffledAccounts.size - 1))
    return ETHTransaction(nonce     = ++account.nonce,
                          from      = account.address,
                          to        = destAccount.address,
                          amount    = Random().nextInt(account.balance) + 1,
                          gasPrice  = gasPrice,
                          gasLimit  = gasLimit,
                          data      = null)
  }

  override fun handlePropagateBlock(block: ETHBlock) {

  }

  override fun handlePropagateTransaction(tx: ETHTransaction) {

  }

  override fun handlePropagateTransactions(txs: List<ETHTransaction>) {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
