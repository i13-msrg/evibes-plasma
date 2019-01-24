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

  var clientsAddressesMap = mutableMapOf<String, MutableList<String>>()
  var confirmedAddressesMap = mutableMapOf<String, Int>()
  val addressMe = UUID.randomUUID()
  var deployedVerticles = mutableListOf<String>()
  var transactionGenerationRateInMillis: Long = 10000

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaManager::class.java)
    private var periodicalMap = mutableMapOf<String, Int>()
    private var NumberOfBlocks = 10
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("Plasma Manager deployed!")

    val numberOfPlasmaClients: Int                  = config().getInteger("numberOfPlasmaClients")
    val mainPlasmaChainAddress: String              = config().getString("mainPlasmaChainAddress")
    val childrenPlasmaChainAddresses: JsonArray     = config().getJsonArray("plasmaChildrenAddresses")
    val numberOfTransactionGenerationIntervals: Int = config().getInteger("numberOfTransactionGenerationIntervals")
    val transactionGenerationRate: Int            = config().getInteger("transactionGenerationRate")

    transactionGenerationRateInMillis = (transactionGenerationRate * 1000).toLong()
    // deploy children plasma chains
    childrenPlasmaChainAddresses.forEach { obj ->
      val childAddress = obj.toString()
      println("childAddress: $childAddress")
      consumersPerChain(childAddress, numberOfPlasmaClients)
      deployPlasma(childAddress, numberOfPlasmaClients, config().copy(), mainPlasmaChainAddress)
      periodicalMap.put(childAddress, numberOfTransactionGenerationIntervals)
    }
    if(childrenPlasmaChainAddresses.size() > 0)
      config().put("childrenPlasmaChainAddresses", childrenPlasmaChainAddresses)
    // deploy plasma main chain
    consumersPerChain(mainPlasmaChainAddress, numberOfPlasmaClients)
    deployPlasma(mainPlasmaChainAddress, numberOfPlasmaClients, config().copy(), null)
    periodicalMap.put(mainPlasmaChainAddress, numberOfTransactionGenerationIntervals)

  }

  fun deployPlasma(chainAddress: String, numberOfPlasmaClients: Int, config: JsonObject, parentPlasmaAddress: String?) {
    config.put("chainAddress", chainAddress)

    if(parentPlasmaAddress != null) {
      config.put("parentPlasmaAddress", parentPlasmaAddress)
    }

    // Deploy ClientsDiscoveryNode
    vertx.deployVerticle("io.plasmasimulator.plasma.verticles.ClientsDiscoveryNode",
      DeploymentOptions().setWorker(true).setConfig(
        JsonObject()
          .put("numberOfClients", numberOfPlasmaClients)
          .put("chainAddress", chainAddress)
        )
    ) { ar ->
      deployedVerticles.add(ar.result())
      // Deploy Operator
      vertx.deployVerticle("io.plasmasimulator.plasma.verticles.Operator",
        DeploymentOptions().setWorker(true).setConfig((config))) { ar ->
        if(ar.failed()) {
          LOG.info(ar.cause().toString())
        }
        deployedVerticles.add(ar.result())
      }

      // Deploy PlasmaClients
      vertx.deployVerticle("io.plasmasimulator.plasma.verticles.PlasmaClient",
        DeploymentOptions().setWorker(true).setInstances(numberOfPlasmaClients).setConfig(config)) { ar ->
        if(ar.failed()) {
          LOG.info(ar.cause().toString())
        }
        deployedVerticles.add(ar.result())
      }
    }
  }

  fun consumersPerChain(chainAddress: String, numberOfPlasmaClients: Int) {
    vertx.eventBus().consumer<Any>("$chainAddress/${Address.PUSH_ALL_ADDRESSES.name}") { msg ->
      LOG.info("GOT ALL ADDRESSES [$chainAddress]")
      val clientsAddresses = mutableListOf<String>()
      val allClientsAddresses = (msg.body() as JsonArray).toMutableList()
      clientsAddresses.addAll(allClientsAddresses.map { address -> address.toString() })
      clientsAddressesMap.put(chainAddress, clientsAddresses)

    }

    confirmedAddressesMap.put(chainAddress, 0)
    vertx.eventBus().consumer<Any>("$chainAddress/${Address.RECEIVED_ALL_ADDRESSES.name}") { msg ->
      LOG.info("RECEIVED FROM ${msg.body().toString()}")
      LOG.info("CHAIN ADDRESS: $chainAddress")
      if(confirmedAddressesMap.containsKey(chainAddress)) {
        var confirmedAddresses: Int? = confirmedAddressesMap.get(chainAddress)
        if(confirmedAddresses != null) {
          confirmedAddresses ++
          confirmedAddressesMap.put(chainAddress, confirmedAddresses)
        }
        if(confirmedAddressesMap.get(chainAddress) == numberOfPlasmaClients) {
          LOG.info("$chainAddress confirmed")
          startPeriodicCall(chainAddress)
        }
      } else {
        println("chainAddress not there")
      }
    }
  }

  fun startPeriodicCall(chainAddress: String) {
    vertx.setPeriodic(transactionGenerationRateInMillis) {id ->
      if(periodicalMap.contains(chainAddress)) {
        var numberOfBlocks = periodicalMap.get(chainAddress)
        if(numberOfBlocks != null) {
          if(numberOfBlocks == 0) {
            vertx.eventBus().send("$chainAddress/${Address.PRINT_BALANCE_FOR_EACH_CLIENT.name}", "")
            LOG.info("Withdrawing funds for all clients ...")
            vertx.eventBus().publish("$chainAddress/${Address.WITHDRAW_TOKENS.name}", "")
            vertx.cancelTimer(id)
            vertx.eventBus().send(Address.STOP_SIMULATION.name, "stop")
          }
          else {
            vertx.eventBus().publish("$chainAddress/${Address.ISSUE_TRANSACTION.name}", Message.ISSUE_TRANSACTION.name)
            numberOfBlocks--
            periodicalMap.put(chainAddress, numberOfBlocks)
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
