package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.conf.Message
import io.plasmasimulator.plasma.models.*
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

class PlasmaManager: AbstractVerticle() {

  var clientsAddresses = mutableListOf<String>()
  val chain = PlasmaChain()

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

    }
    vertx.eventBus().consumer<Any>(Address.PUSH_ALL_ADDRESSES.name) { msg ->
      LOG.info("GOT ALL ADDRESSES")
      val allClientsAddresses = (msg.body() as JsonArray).toMutableList()
      clientsAddresses.addAll(allClientsAddresses.map { address -> address.toString() })
    }

    vertx.eventBus().consumer<Any>(Address.GENESIS_PLASMA_BLOCK_ADDED.name) {
      createBlockTransactionForEachClient()
      vertx.setPeriodic(10000) {id ->
        broadcast()
      }
    }
  }

  fun bootstrapBlockchain() {
    val genesisBlock = PlasmaBlock(number = 0, prevBlockNum = 0, prevBlockHash = mutableListOf<Byte>().toByteArray())
    val genesisBlockJson  = JsonObject(Json.encode(genesisBlock))
    chain.addBlock(genesisBlock, UTXOPool())
    vertx.eventBus().publish(Address.GENESIS_PLASMA_BLOCK.name, genesisBlockJson)
  }

  fun createBlockTransactionForEachClient() {
      for (address in clientsAddresses) {
        vertx.eventBus().send(Address.DEPOSIT_TRANSACTION.name, JsonObject(Json.encode(createTransaction(address))))
      }
  }

  fun createTransaction(address: String) : Transaction{
    val tx = Transaction()
    tx.depositTransaction = true
    // TODO: set transaction amount via config
    tx.addOutput(address, 10)
    return tx
  }

  fun broadcast() {
    vertx.eventBus().publish(Address.ISSUE_TRANSACTION.name, Message.ISSUE_TRANSACTION.name)
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }
}
