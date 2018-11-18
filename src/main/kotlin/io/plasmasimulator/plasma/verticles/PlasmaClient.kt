package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.SimulationManagerVerticle
import io.plasmasimulator.conf.Address
import io.plasmasimulator.conf.Message
import io.plasmasimulator.plasma.models.PlasmaChain
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import org.slf4j.LoggerFactory
import java.util.*

class PlasmaClient: PlasmaParticipant() {

  var allOtherClientsAddresses = mutableListOf<String>()

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaClient::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("Here is my address $address")

    vertx.eventBus().send<Any>(Address.PUBLISH_ADDRESS.name, address) { response ->
      LOG.info("SUCCESS")
    }

    vertx.eventBus().consumer<Any>(Address.PUSH_ALL_ADDRESSES.name) { msg ->
      LOG.info("GOT ALL ADDRESSES")
      val allClientsAddresses = (msg.body() as JsonArray).toMutableList()

      allOtherClientsAddresses.addAll(allClientsAddresses
                              .filter { clientAddress -> clientAddress != this.address  }
                              .map { address -> address.toString() })
    }

    vertx.eventBus().consumer<Any>(Address.PLASMA_BROADCAST.name) { msg ->
      LOG.info("BROADCAST MESSAGE RECEIVED")
      when(msg.body()) {
        Message.ISSUE_TRANSACTION.name -> {
          send()
          msg.reply("DONE")}
      }
    }
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }

  fun receive() {

  }

  fun send() {
    var amount = Random().nextInt(balance)
    var address = allOtherClientsAddresses.get(Random().nextInt(allOtherClientsAddresses.size))

  }

}
