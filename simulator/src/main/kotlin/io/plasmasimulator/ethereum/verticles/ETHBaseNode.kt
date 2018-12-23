package io.plasmasimulator.ethereum.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.ethereum.models.ETHBlock
import io.plasmasimulator.ethereum.models.ETHChain
import io.plasmasimulator.ethereum.models.ETHTransaction
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.util.*

open class ETHBaseNode : AbstractVerticle() {
  val ethAddress = ETHBaseNode.addressNum++.toString()
  var txPool = LinkedList<ETHTransaction>()
  var ethChain = ETHChain()

  private companion object {
    private val LOG = LoggerFactory.getLogger(ETHBaseNode::class.java)
    private var addressNum = 0
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    vertx.eventBus().send(Address.ETH_PUBLISH_ADDRESS.name, ethAddress)
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }
}
