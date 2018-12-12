package io.plasmasimulator

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.plasmasimulator.conf.Address
import io.plasmasimulator.conf.Configuration
import io.plasmasimulator.conf.Message
import io.vertx.config.ConfigRetriever
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import org.slf4j.LoggerFactory
import java.util.*

class SimulationManagerVerticle : AbstractVerticle() {
  private var deployedVerticleIds = mutableListOf<String>()
  private companion object {
    private val LOG = LoggerFactory.getLogger(SimulationManagerVerticle::class.java)
  }

  override fun start(startFuture: Future<Void>) {
    // module for parsing json to objects
    Json.mapper.registerModule(KotlinModule())
    consumers()
    var router = Router.router(vertx)

    val sockJSHandler = SockJSHandler.create(vertx)

    val options = BridgeOptions()
      .addInboundPermitted(PermittedOptions().setAddress(Address.PUSH_ALL_ADDRESSES.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.PUSH_ALL_ADDRESSES.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.CONFIGURE_SIMULATION.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.CONFIGURE_SIMULATION.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.GET_CONFIGURATION.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.GET_CONFIGURATION.name))

    sockJSHandler.bridge(options)

    router.route("/eventbus/*").handler(sockJSHandler)
    router.route("/start").handler { req ->
      LOG.info("Starting simulation from API")
      val responseMsg = if(startSimulation())
        "Simulation started!"
      else
        "Simulation is running! Please stop id first in order to restart it."

      req.response()
        .putHeader("content-type", "text/plain")
        .end(responseMsg)
    }
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

      val updateConfig = req.body as JsonObject

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
      LOG.info("received GET CONFIG from APP");
      msg.reply(Configuration.configJSON);
    }
    vertx.eventBus().consumer<Any>(Address.CONFIGURE_SIMULATION.name) { msg ->
      LOG.info("received configuration from APP")
      configureSimulationWith(msg.body() as JsonObject)
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

  fun startSimulation(): Boolean {
    if(deployedVerticleIds.size > 0) return false
    val confOptions = Configuration.getConfigRetrieverOptions()
    var retriever = ConfigRetriever.create(vertx, confOptions)
    retriever.getConfig() { ar ->
      var conf = ar.result()
      var opt = DeploymentOptions().setWorker(true).setInstances(conf.getInteger("numberOfEthereumNodes", 1))

      vertx.deployVerticle("io.plasmasimulator.ethereum.verticles.ETHNodeVerticle", opt) { ar ->
        deployedVerticleIds.add(ar.result())
      }

      val plasmaManagerConfig = JsonObject()
        .put("numberOfPlasmaClients", conf.getInteger("numberOfPlasmaClients"))
        .put("plasmaContractAddress", UUID.randomUUID().toString())
        .put("amountPerClient", conf.getInteger("tokensPerClient"))

      vertx.deployVerticle("io.plasmasimulator.plasma.verticles.PlasmaManager",
        DeploymentOptions().setWorker(true).setInstances(1).setConfig(plasmaManagerConfig)) { ar ->
        deployedVerticleIds.add(ar.result())
      }

      vertx.eventBus().publish(Address.ETH_NODES_BROADCAST.name,
        JsonObject().put("type", Message.ISSUE_TRANSACTION.name))

      vertx.eventBus().send(Address.RUN_PLASMA_CHAIN.name,
        JsonObject()
          .put("numberOfPlasmaClients", conf.getInteger("numberOfPlasmaClients"))
          .put("plasmaContractAddress", UUID.randomUUID().toString())
          .put("amountPerClient", conf.getInteger("amountPerClient")))
    }
    return true
  }

  fun stopSimulation(): Boolean {
    if(deployedVerticleIds.size < 1) return false // nothing deployed yet

    deployedVerticleIds.forEach { id ->
      vertx.undeploy(id)
    }

    deployedVerticleIds.clear()
    return true
  }

  fun matchParams(currentConfig: JsonObject, updateConfig: JsonObject) : String {
    currentConfig.forEach { item ->
      if(!updateConfig.containsKey(item.key))
        return item.key
    }
    return Message.SUCCESS.name
  }
}
