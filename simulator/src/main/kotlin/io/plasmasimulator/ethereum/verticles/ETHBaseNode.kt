package io.plasmasimulator.ethereum.verticles

import io.plasmasimulator.conf.Address
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

abstract class ETHBaseNode : AbstractVerticle() {
  val ethAddress = ETHBaseNode.addressNum++.toString()
  var txPool = LinkedList<ETHTransaction>()
  var ethChain = ETHChain()
  var peers = mutableListOf<String>()
  var batchNumbers = mutableListOf<Int>()

  private companion object {
    private val LOG = LoggerFactory.getLogger(ETHBaseNode::class.java)
    private var addressNum = 0
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    vertx.eventBus().send(Address.ETH_PUBLISH_ADDRESS.name, ethAddress)
    startConsumers()
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }

  fun startConsumers() {
    vertx.eventBus().consumer<Any>(ethAddress) { msg ->
      val jsonObject = msg.body() as JsonObject
      when(jsonObject.getString("type")) {

        "propagateTransaction" -> {
          val txJson = jsonObject.getJsonObject("transaction")
          val tx: ETHTransaction = Json.decodeValue(txJson.toString(), ETHTransaction::class.java)
          handlePropagateTransaction(tx)
        }

        "propagateTransactions" -> {
          val batchNumber = jsonObject.getInteger("batchNumber")
          if(!batchNumbers.contains(batchNumber)) {
            batchNumbers.add(batchNumber)

            val txJsonArray: JsonArray = jsonObject.getJsonArray("transactions")
            val transactions: MutableList<ETHTransaction> = mutableListOf()

            txJsonArray.forEach { txJson ->
              val tx: ETHTransaction = Json.decodeValue(txJson.toString(), ETHTransaction::class.java)
              transactions.add(tx)
            }
            handlePropagateTransactions(transactions)
            propagateTransactions(jsonObject)
          }
        }

        "propagateBlock" -> {
          val blockJson = jsonObject.getJsonObject("block")
          val block: ETHBlock = Json.decodeValue(blockJson.toString(), ETHBlock::class.java)
          handlePropagateBlock(block)
        }

        "setNewPeers" -> {
          handleSetNewPeers(jsonObject.getJsonArray("peers"))
        }
      }

    }
  }

  abstract fun handlePropagateBlock(block: ETHBlock)
  abstract fun handlePropagateTransaction(tx: ETHTransaction)
  abstract fun handlePropagateTransactions(txs: List<ETHTransaction>)

  fun propagateTransactions(data: JsonObject) {
    sendToPeers(data)
  }

  fun sendToPeers(data: JsonObject) {
    for( peer in peers) {
      vertx.eventBus().send(peer, data)
    }
  }

  open fun handleSetNewPeers(newPeers: JsonArray) {
    peers.clear()
    newPeers.forEach { peer ->
      peers.add(peer.toString())
    }
  }

}
