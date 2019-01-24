package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.conf.Message
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

class ClientsDiscoveryNode: AbstractVerticle() {
  var clientsAddresses = JsonArray()
  var balanceMap = mutableMapOf<String, Int>()

  companion object {
      private val LOG = LoggerFactory.getLogger(ClientsDiscoveryNode::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    val numberOfClients = config().getInteger("numberOfClients")
    val chainAddress = config().getString("chainAddress")

    LOG.info("ClientsDiscoveryNode for $chainAddress deployed")

    vertx.eventBus().consumer<Any>("$chainAddress/${Address.PUBLISH_ADDRESS.name}") { msg ->
      val newAddress = msg.body() as String
      if(!clientsAddresses.contains(newAddress))
        clientsAddresses.add(newAddress)

      if(clientsAddresses.size() == numberOfClients){

        vertx.eventBus().publish("$chainAddress/${Address.PUSH_ALL_ADDRESSES.name}", clientsAddresses)
      }
      msg.reply(Message.SUCCESS.name)
    }

    vertx.eventBus().consumer<Any>("$chainAddress/${Address.PUBLISH_BALANCE.name}") { msg ->
      val jsonbObject = msg.body() as JsonObject
      balanceMap.put(jsonbObject.getString("address"), jsonbObject.getInteger("balance"))
    }

    vertx.eventBus().consumer<Any>("$chainAddress/${Address.PRINT_BALANCE_FOR_EACH_CLIENT.name}") {
      var total = 0
      balanceMap.forEach{(address, balance) ->
        println("$address has $balance")
        total += balance
      }
      LOG.info("[$chainAddress] TOTAL SUM: $total")
    }
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }
}
