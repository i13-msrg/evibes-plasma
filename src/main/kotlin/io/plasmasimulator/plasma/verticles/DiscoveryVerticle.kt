package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.conf.Message
import io.plasmasimulator.plasma.models.UTXO
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.*

class DiscoveryVerticle: AbstractVerticle() {
  var clientsAddresses = JsonArray()
  var balanceMap = mutableMapOf<String, Int>()

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    val numberOfClients = config().getInteger("numberOfClients")
    val balancePerClient = config().getInteger("balance")

    vertx.eventBus().consumer<Any>(Address.PUBLISH_ADDRESS.name) { msg ->
      val newAddress = msg.body() as String
      if(!clientsAddresses.contains(newAddress))
        clientsAddresses.add(newAddress)
      //val utxo = UTXO()

      if(clientsAddresses.size() == numberOfClients){
        vertx.eventBus().publish(Address.PUSH_ALL_ADDRESSES.name, clientsAddresses)
      }
      msg.reply(Message.SUCCESS.name)
    }

    vertx.eventBus().consumer<Any>(Address.PUBLISH_BALANCE.name) { msg ->
      val jsonbObject = msg.body() as JsonObject
      balanceMap.put(jsonbObject.getString("address"), jsonbObject.getInteger("balance"))
    }

    vertx.eventBus().consumer<Any>(Address.PRINT_BALANCE_FOR_EACH_CLIENT.name) {
      var total = 0
      balanceMap.forEach{(address, balance) ->
        println("$address has $balance")
        total += balance
      }
      println("TOTAL SUM: $total")
    }

//    vertx.eventBus().consumer<Any>(Address.REQUEST_ADDRESS.name) { msg ->
//      val requesterAddress = msg.body()
//      val remainingAddreses = clientsAddresses.filter { address -> address != requesterAddress }
//      msg.reply(remainingAddreses.get(Random().nextInt(remainingAddreses.size)))
//    }
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }
}
