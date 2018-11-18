package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.SimulationManagerVerticle
import io.plasmasimulator.conf.Address
import io.plasmasimulator.plasma.models.PlasmaChain
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import org.slf4j.LoggerFactory

class PlasmaClient: PlasmaParticipant() {

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaClient::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("Hello from Client")
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }

  fun receive() {

  }

}
