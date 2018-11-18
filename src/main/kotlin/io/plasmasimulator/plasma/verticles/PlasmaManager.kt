package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.conf.Address
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
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

      // Deploy Operator
      val config = JsonObject().put("plasmaContractAddress", plasmaContractAddress).put("balance", balance)

      vertx.deployVerticle("io.plasmasimulator.plasma.verticles.Operator", DeploymentOptions().setWorker(true).setConfig((config)))

      // Deploy PlasmaClients
      var opt = DeploymentOptions().setWorker(true).setInstances(numberOfPlasmaClients).setConfig(config)
      vertx.deployVerticle("io.plasmasimulator.plasma.verticles.PlasmaClient", opt)

      //vertx.eventBus().publish(Address.SET_PLASMA_CONTRACT_ADDRESS.name, plasmaContractAddress)

    }
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }
}
