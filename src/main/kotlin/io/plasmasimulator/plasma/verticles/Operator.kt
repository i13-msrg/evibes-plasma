package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasma.models.PlasmaChain
import io.plasmasimulator.utils.MerkleTreeBuilder
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import org.slf4j.LoggerFactory

class Operator: PlasmaParticipant() {

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaParticipant::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)


  }

  fun applyBlock(block: PlasmaBlock) : Boolean {
    if(!chain.validateBlock(block)) return false
    val blockRoot = MerkleTreeBuilder.getRoot(block.transactions)
    send(Address.APPLY_BLOCK.name, blockRoot.digest)
    return true
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }

  fun send(address: String, msg: Any?) {
    vertx.eventBus().publish(address,
      msg)
  }

}
