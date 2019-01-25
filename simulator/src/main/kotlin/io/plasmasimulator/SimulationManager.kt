package io.plasmasimulator

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.plasmasimulator.conf.Address
import io.plasmasimulator.conf.BridgeOptionsConfig
import io.plasmasimulator.conf.Configuration
import io.plasmasimulator.conf.Message
import io.vertx.config.ConfigRetriever
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import org.slf4j.LoggerFactory
import java.util.*

class SimulationManager : AbstractVerticle() {
  private var deployedVerticleIds = mutableListOf<String>()
  private var startTime : Long = 0
  private companion object {
    private val LOG = LoggerFactory.getLogger(SimulationManager::class.java)
  }

  override fun start(startFuture: Future<Void>) {
    // module for parsing json to objects
    Json.mapper.registerModule(KotlinModule())
    consumers()
    var router = Router.router(vertx)

    val sockJSHandler = SockJSHandler.create(vertx)

    val options = BridgeOptionsConfig.getOptions()

    sockJSHandler.bridge(options)

    router.route("/eventbus/*").handler(sockJSHandler)
    router.route("/stop").handler { req ->
      LOG.info("Stopping simulation from API")
      val responseMsg = if(stopSimulation())
        "Simulation stopped!"
      else
        "Simulation is not running!"
      req.response()
        .putHeader("content-type", "text/plain")
        .end(responseMsg)
    }

    router.route("/configure").handler { req ->
      req.response()
        .putHeader("content-type", "text/plain")
        .end("CONFIGURE")
    }
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8080) { http ->
        if (http.succeeded()) {
          startFuture.complete()
          LOG.info("HTTP server started on port 8080")
        } else {
          startFuture.fail(http.cause())
        }
      }
  }

  fun consumers() {
    vertx.eventBus().consumer<Any>(Address.GET_CONFIGURATION.name) { msg ->
      msg.reply(Configuration.configJSON);
    }
    vertx.eventBus().consumer<Any>(Address.UPDATE_CONFIGURATION.name) { msg ->
      LOG.info("Received new configuration")
      LOG.info(msg.body().toString())
      configureSimulationWith(msg.body() as JsonObject)
      msg.reply("SUCCESS")
    }

    vertx.eventBus().consumer<Any>(Address.START_SIMULATION.name) { msg ->
      LOG.info("Starting simulation ...")
      LOG.info(msg.body().toString())
      startSimulation(msg.body() as JsonObject)
      msg.reply("SUCCESS")
    }

    vertx.eventBus().consumer<Any>(Address.STOP_SIMULATION.name) { msg ->
      LOG.info("Stopping simulation ...");
      stopSimulation()
      msg.reply("SUCCESS")
    }

  }
  fun configureSimulationWith(updateConfig: JsonObject) : String {
    val currentConfig = Configuration.configJSON
    val result = matchParams(currentConfig, updateConfig)
    if(result == Message.SUCCESS.name) {
      Configuration.setConfigRetrieverOptions(updateConfig)
      return Message.SUCCESS.name
    }
    else
      return result
  }

  fun startSimulation(chainAddresses: JsonObject): Boolean {
    if(deployedVerticleIds.size > 0) return false
    startTime = System.currentTimeMillis()
    val confOptions = Configuration.getConfigRetrieverOptions()
    var retriever = ConfigRetriever.create(vertx, confOptions)
    retriever.getConfig() { ar ->
      var conf = ar.result()

      val mainPlasmaChainAddress = chainAddresses.getString("mainPlasmaChainAddress")
      val plasmaChildren = conf.getInteger("plasmaChildren")
      val plasmaChildrenAddresses = JsonArray()
      if(plasmaChildren > 0) {
        chainAddresses.getJsonArray("plasmaChildrenAddresses").forEach { address ->
          plasmaChildrenAddresses.add(address)
        }
      }
      // Deploy plasma clients
      val plasmaManagerConfig = JsonObject()
        .put("numberOfPlasmaClients", conf.getInteger("numberOfPlasmaClients"))
        .put("plasmaContractAddress", "contract-${UUID.randomUUID().toString()}")
        .put("amountPerClient", conf.getInteger("tokensPerClient"))
        .put("transactionsPerBlock", conf.getInteger("transactionsPerPlasmaBlock"))
        .put("mainPlasmaChainAddress", mainPlasmaChainAddress)
        .put("plasmaChildrenAddresses", plasmaChildrenAddresses)
        .put("plasmaBlockInterval", conf.getInteger("plasmaBlockInterval"))
        .put("plasmaTXGasLimit", conf.getInteger("plasmaTXGasLimit"))
        .put("transactionGenerationRate", conf.getInteger("transactionGenerationRate"))
        .put("numberOfTransactionGenerationIntervals", conf.getInteger("numberOfTransactionGenerationIntervals"))

      vertx.deployVerticle("io.plasmasimulator.ethereum.verticles.PeerDiscoveryNode",
        DeploymentOptions()
          .setWorker(true)
          .setConfig(conf)
          .setInstances(1)){ ar ->
        if(ar.failed()) {
          LOG.info(ar.cause().toString())
        }
        deployedVerticleIds.add(ar.result())
        // Deploy eth nodes
        vertx.deployVerticle("io.plasmasimulator.ethereum.verticles.EthereumManager",
          DeploymentOptions().setWorker(true).setInstances(1).setConfig(conf)) { ar ->
          if(ar.failed()) {
            println(ar.cause())
          }
          deployedVerticleIds.add(ar.result())
        }
        // Deploy plasma manager
        vertx.deployVerticle("io.plasmasimulator.plasma.verticles.PlasmaManager",
          DeploymentOptions().setWorker(true).setInstances(1).setConfig(plasmaManagerConfig)) { ar ->
          if(ar.failed()) {
            println(ar.cause())
          }
          deployedVerticleIds.add(ar.result())
        }
        // Deploy reducer
        vertx.deployVerticle("io.plasmasimulator.Reducer",
          DeploymentOptions().setWorker(true).setInstances(1).setConfig(plasmaManagerConfig)) { ar ->
          if(ar.failed()){
            LOG.info("Reducer deployment failed")
            println(ar.cause())
          }
          deployedVerticleIds.add(ar.result())
        }
      }
    }
    return true
  }

  fun stopSimulation(): Boolean {
    if(deployedVerticleIds.size < 1) return false // nothing deployed yet

    deployedVerticleIds.forEach { id ->
      vertx.undeploy(id)
    }
    val stopTime = System.currentTimeMillis()
    val duration = stopTime - startTime
    val minutes = duration / 1000 / 60
    val seconds = (duration / 1000) % 60
    LOG.info("Simulation time: $minutes:$seconds")
    deployedVerticleIds.clear()
    vertx.eventBus().send(Address.SIMULATION_STOPPED.name, "")
    return true
  }

  fun generateAddreses(number: Int) : JsonArray {
    var addresses = JsonArray()
    for (i in 0 until number) {
      addresses.add(UUID.randomUUID().toString());
    }
    return addresses
  }

  fun matchParams(currentConfig: JsonObject, updateConfig: JsonObject) : String {
    currentConfig.forEach { item ->
      if(!updateConfig.containsKey(item.key))
        return item.key
    }
    return Message.SUCCESS.name
  }
}
