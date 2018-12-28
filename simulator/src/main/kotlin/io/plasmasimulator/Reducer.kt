package io.plasmasimulator

import io.plasmasimulator.conf.Address
import io.plasmasimulator.ethereum.models.ETHBlock
import io.plasmasimulator.ethereum.models.ETHTransaction
import io.plasmasimulator.ethereum.verticles.ETHBaseNode
import io.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasma.models.Transaction
import io.plasmasimulator.plasma.verticles.PlasmaParticipant
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.util.*

class Reducer: ETHBaseNode() {
  var balanceMap = mutableMapOf<String, Int>()

  private companion object {
    private val LOG = LoggerFactory.getLogger(Reducer::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    val mainPlasmaChainAddress: String = config().getString("mainPlasmaChainAddress")
    val plasmaChildrenAddresses: JsonArray = config().getJsonArray("plasmaChildrenAddresses")
    LOG.info("Reducer deployed")

    vertx.eventBus().consumer<Any>("$mainPlasmaChainAddress/${Address.PUBLISH_BLOCK.name}") { msg ->
      //LOG.info("Send block to app")
      vertx.eventBus().send(Address.ADD_NEW_MAIN_PLASMA_BLOCK.name, msg.body())
    }
    consumers(mainPlasmaChainAddress)

    plasmaChildrenAddresses.forEach { childAddress ->
      vertx.eventBus().consumer<Any>("$childAddress/${Address.PUBLISH_BLOCK.name}") { msg ->
        val block: PlasmaBlock = Json.decodeValue(msg.body().toString(), PlasmaBlock::class.java)
        //LOG.info("Send child block ${block.number} to app [$childAddress]")
        val data: JsonObject = JsonObject().put("chainAddress", childAddress).put("childBlock", msg.body())
        vertx.eventBus().send(Address.ADD_NEW_CHILD_PLASMA_BLOCK.name, data)
      }
      consumers(childAddress as String)
    }


  }

  fun consumers(chainAddress: String) {
    vertx.eventBus().consumer<Any>("$chainAddress/${Address.PUBLISH_TRANSACTION.name}") { msg ->
      val data: JsonObject = JsonObject().put("chainAddress", chainAddress)
      vertx.eventBus().send(Address.PLASMA_TRANSACTION_PUBLISHED.name, data)
    }

    vertx.eventBus().consumer<Any>("$chainAddress/${Address.ETH_ANNOUNCE_DEPOSIT.name}") { msg ->
      val data: JsonObject = JsonObject().put("chainAddress", chainAddress)
      vertx.eventBus().send(Address.DEPOSIT_TRANSACTION_PUBLISHED.name, data)
    }
  }

  override fun handlePropagateBlock(block: ETHBlock) {
    if (ethChain.containsBlock(block.number)) {
      LOG.info("[$ethAddress]: attempted to add block ${block.number}, but it already exists!")
    } else {
      ethChain.addBlock(block)
      // send the new block to web app
      vertx.eventBus().send(Address.ADD_ETH_BLOCK.name, JsonObject(Json.encode(block)))
      //LOG.info("[REDUCER]: added block ${block.number}")
    }
  }

    override fun handlePropagateTransaction(tx: ETHTransaction) {
      if(!txPool.contains(tx)) {
        txPool.push(tx)
        vertx.eventBus().send(Address.ADD_ETH_TRANSACTION.name, JsonObject(Json.encode(tx)))
      }
    }
  }
