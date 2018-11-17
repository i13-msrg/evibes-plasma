package io.plasmasimulator.plasmasimulator.plasma.verticles

import io.plasmasimulator.plasmasimulator.SimulationManagerVerticle
import io.plasmasimulator.plasmasimulator.conf.Address
import io.plasmasimulator.plasmasimulator.conf.Message
import io.plasmasimulator.plasmasimulator.ethereum.ETHNodeVerticle
import io.plasmasimulator.plasmasimulator.plasma.models.PlasmaChain
import io.plasmasimulator.plasmasimulator.plasma.models.PlasmaParticipant
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

class PlasmaClient(override val chain: PlasmaChain,
                   override val address: String,
                   override val plasmaContractAddress: String): AbstractVerticle(), PlasmaParticipant {
  private companion object {
    private val LOG = LoggerFactory.getLogger(SimulationManagerVerticle::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }

  fun receive() {

  }

}
