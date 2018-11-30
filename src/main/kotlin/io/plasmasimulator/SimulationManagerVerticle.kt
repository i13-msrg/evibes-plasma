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
import org.slf4j.LoggerFactory
import java.util.*

class SimulationManagerVerticle : AbstractVerticle() {
  private companion object {
    private val LOGGER = LoggerFactory.getLogger(SimulationManagerVerticle::class.java)
  }

  override fun start(startFuture: Future<Void>) {
    // module for parsing json to objects
    Json.mapper.registerModule(KotlinModule())
    var options = DeploymentOptions().setWorker(true).setInstances(10)

    val confOptions = Configuration.getConfigRetrieverOptions()
    var retriever = ConfigRetriever.create(vertx, confOptions)

    retriever.getConfig(){ar ->
      var conf = ar.result()
      var opt = DeploymentOptions().setWorker(true).setInstances(conf.getInteger("instances", 1))

      vertx.deployVerticle("io.plasmasimulator.ethereum.verticles.ETHNodeVerticle", opt)
      vertx.deployVerticle("io.plasmasimulator.plasma.verticles.PlasmaManager", opt)

      vertx
        .createHttpServer()
        .requestHandler { req ->
          println("SEND MSG")
          println(req.absoluteURI())
          vertx.eventBus().publish(Address.ETH_NODES_BROADCAST.name,
            JsonObject().put("type", Message.ISSUE_TRANSACTION.name))

          vertx.eventBus().send(Address.RUN_PLASMA_CHAIN.name,
            JsonObject().put("numberOfPlasmaClients", 10)
              .put("plasmaContractAddress", UUID.randomUUID().toString())
              .put("amount", 10))

          req.response()
            .putHeader("content-type", "text/plain")
            .end("Hello from Plasma Simulator!")
        }
        .listen(8080) { http ->
          if (http.succeeded()) {
            startFuture.complete()
            LOGGER.info("HTTP server started on port 8080")
          } else {
            startFuture.fail(http.cause())
          }
        }
    }

  }

}
