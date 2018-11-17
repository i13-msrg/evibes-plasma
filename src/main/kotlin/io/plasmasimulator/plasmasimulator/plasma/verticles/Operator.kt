package io.plasmasimulator.plasmasimulator.plasma.verticles

import io.plasmasimulator.plasmasimulator.conf.Address
import io.plasmasimulator.plasmasimulator.conf.Message
import io.plasmasimulator.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasmasimulator.plasma.models.PlasmaChain
import io.plasmasimulator.plasmasimulator.plasma.models.PlasmaParticipant
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject

class Operator(override val chain: PlasmaChain,
               override val address: String,
               override val plasmaContractAddress: String): AbstractVerticle(), PlasmaParticipant {

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)

  }

  fun applyBlock(block: PlasmaBlock) : Boolean {
    if(!chain.validateBlock(block)) return false
    send(Address.APPLY_BLOCK.name, block)
    return true
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }

  fun send(address: String, msg: Any) {
    vertx.eventBus().publish(address,
      msg)
  }

}
