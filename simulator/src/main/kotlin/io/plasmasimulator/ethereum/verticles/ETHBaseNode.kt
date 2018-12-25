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

  open fun handleSetNewPeers(newPeers: JsonArray) {
    LOG.info("[$ethAddress] received new peers of size: ${newPeers.size()}")
    peers.clear()
    newPeers.forEach { peer ->
      peers.add(peer.toString())
    }
  }

}
