package io.plasmasimulator

import io.plasmasimulator.conf.Address
import io.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasma.verticles.PlasmaParticipant
import io.vertx.core.Future
import io.vertx.core.json.Json
import org.slf4j.LoggerFactory

class Reducer: PlasmaParticipant() {

  private companion object {
    private val LOG = LoggerFactory.getLogger(Reducer::class.java)
  }
  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)

    vertx.eventBus().consumer<Any>(Address.PUBLISH_BLOCK.name) { msg ->
      LOG.info("Send block to app")
      vertx.eventBus().send(Address.ADD_NEW_BLOCK.name, msg.body())
    }
  }
}
