package io.plasmasimulator

import io.plasmasimulator.conf.Address
import io.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasma.verticles.PlasmaParticipant
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import org.slf4j.LoggerFactory

class Reducer: AbstractVerticle() {

  private companion object {
    private val LOG = LoggerFactory.getLogger(Reducer::class.java)
  }
  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    val mainPlasmaChainAddress: String = config().getString("mainPlasmaChainAddress")
    val plasmaChildrenAddresses: JsonArray = config().getJsonArray("plasmaChildrenAddresses")
    LOG.info("Reducer deployed")

    vertx.eventBus().consumer<Any>("$mainPlasmaChainAddress/${Address.PUBLISH_BLOCK.name}") { msg ->
      LOG.info("Send block to app")
      vertx.eventBus().send("$mainPlasmaChainAddress/${Address.ADD_NEW_BLOCK.name}", msg.body())
    }

    plasmaChildrenAddresses.forEach { childAddress ->
      vertx.eventBus().consumer<Any>("$childAddress/${Address.PUBLISH_BLOCK.name}") { msg ->
        LOG.info("Send block to app")
        vertx.eventBus().send("$childAddress/${Address.ADD_NEW_BLOCK.name}", msg.body())
      }
    }
  }
}
