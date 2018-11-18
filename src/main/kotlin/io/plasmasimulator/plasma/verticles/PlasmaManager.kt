package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.conf.Message
import io.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasma.models.PlasmaChain
import io.plasmasimulator.plasma.models.Transaction
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

class PlasmaManager: AbstractVerticle() {

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaManager::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    vertx.eventBus().consumer<Any>(Address.RUN_PLASMA_CHAIN.name) { msg ->
      val jsonObject= msg.body() as JsonObject
      LOG.info("RECEIVED MESSAGE")

      val numberOfPlasmaClients = jsonObject.getInteger("numberOfPlasmaClients")
      val plasmaContractAddress = jsonObject.getString("plasmaContractAddress")
      val balance = jsonObject.getInteger("amount")


      val config = JsonObject().put("plasmaContractAddress", plasmaContractAddress).put("balance", balance)
      // Deploy DiscoveryVerticle
      vertx.deployVerticle("io.plasmasimulator.plasma.verticles.DiscoveryVerticle",
        DeploymentOptions().setWorker(true).setConfig(JsonObject().put("numberOfClients", numberOfPlasmaClients).put("balance", balance)))
      // Deploy Operator
      vertx.deployVerticle("io.plasmasimulator.plasma.verticles.Operator", DeploymentOptions().setWorker(true).setConfig((config)))

      // Deploy PlasmaClients
      var opt = DeploymentOptions().setWorker(true).setInstances(numberOfPlasmaClients).setConfig(config)
      vertx.deployVerticle("io.plasmasimulator.plasma.verticles.PlasmaClient", opt)

      bootstrapBlockchain()
      //vertx.eventBus().publish(Address.SET_PLASMA_CONTRACT_ADDRESS.name, plasmaContractAddress)
//      vertx.setPeriodic(10000) {id ->
//        broadcast()
//      }

    }
  }

  fun bootstrapBlockchain() {
    val chain = PlasmaChain()
    val genesisBlock = PlasmaBlock(number = 0, prevBlockNum = 0, prevBlockHash = mutableListOf<Byte>().toByteArray())
    val genesisBlockJson  = JsonObject(Json.encode(genesisBlock))
    chain.addBlock(genesisBlock)
    vertx.eventBus().publish(Address.GENESIS_PLASMA_BLOCK.name, genesisBlockJson)
  }

  fun broadcast() {
    vertx.eventBus().send<Any>(Address.PLASMA_BROADCAST.name, Message.ISSUE_TRANSACTION.name) { response ->
      LOG.info("Response: ${response.result().body()}")
    }
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }
}
