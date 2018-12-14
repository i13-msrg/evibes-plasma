package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.conf.Message
import io.plasmasimulator.plasma.models.*
import io.plasmasimulator.utils.FileManager
import io.plasmasimulator.utils.HashUtils
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.util.*

class PlasmaManager: AbstractVerticle() {

  var clientsAddresses = mutableListOf<String>()
  val chain = PlasmaChain()
  val addressMe = UUID.randomUUID()
  var deployedVerticles = mutableListOf<String>()

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaManager::class.java)
    private var NumberOfBlocks = 10
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("Plasma Manager deployed!")

    val numberOfPlasmaClients = config().getInteger("numberOfPlasmaClients")
    val plasmaContractAddress = config().getString("plasmaContractAddress")
    val amountPerClient = config().getInteger("amountPerClient")
    val transactionsPerBlock = config().getInteger("transactionsPerBlock")

    val config = JsonObject().put("plasmaContractAddress", plasmaContractAddress)
                                         .put("amount", amountPerClient)
                                         .put("transactionsPerBlock", transactionsPerBlock)
    // Deploy DiscoveryVerticle
    vertx.deployVerticle("io.plasmasimulator.plasma.verticles.DiscoveryVerticle",
      DeploymentOptions().setWorker(true).setConfig(JsonObject().put("numberOfClients", numberOfPlasmaClients))) { ar ->
      deployedVerticles.add(ar.result())
    }
    // Deploy Operator
    vertx.deployVerticle("io.plasmasimulator.plasma.verticles.Operator",
      DeploymentOptions().setWorker(true).setConfig((config))) { ar ->
      deployedVerticles.add(ar.result())
    }

    // Deploy PlasmaClients
    var opt = DeploymentOptions().setWorker(true).setInstances(numberOfPlasmaClients).setConfig(config)
    vertx.deployVerticle("io.plasmasimulator.plasma.verticles.PlasmaClient", opt) { ar ->
      deployedVerticles.add(ar.result())
    }

    vertx.eventBus().consumer<Any>(Address.RUN_PLASMA_CHAIN.name) { msg ->
      val jsonObject= msg.body() as JsonObject
      LOG.info("RECEIVED MESSAGE")

    }
    vertx.eventBus().consumer<Any>(Address.PUSH_ALL_ADDRESSES.name) { msg ->
      LOG.info("GOT ALL ADDRESSES")
      val allClientsAddresses = (msg.body() as JsonArray).toMutableList()
      clientsAddresses.addAll(allClientsAddresses.map { address -> address.toString() })
    }
    var confirmedClients = 0
    vertx.eventBus().consumer<Any>(Address.RECEIVED_ALL_ADDRESSES.name) { msg ->
      LOG.info("RECEIVED FROM ${msg.body().toString()}")
      confirmedClients ++
      if(confirmedClients == numberOfPlasmaClients) {
        vertx.setPeriodic(10000) {id ->
          if(NumberOfBlocks-- == 0) {
            vertx.eventBus().send(Address.PRINT_BALANCE_FOR_EACH_CLIENT.name, "")
            vertx.cancelTimer(id)
          } else {
            println("send $addressMe")
            vertx.eventBus().publish(Address.ISSUE_TRANSACTION.name, Message.ISSUE_TRANSACTION.name)
          }
        }
      }
    }
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
    LOG.info("Stopping and undeploying all slave verticles")
    // undeploy all verticles -> PlasmaClient, Operator, etc.
    deployedVerticles.forEach{ id ->
      vertx.undeploy(id)
    }
    deployedVerticles.clear()
  }
}
