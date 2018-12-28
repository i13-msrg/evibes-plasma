package io.plasmasimulator.ethereum.verticles

import io.plasmasimulator.conf.Address
import io.plasmasimulator.conf.Message
import io.plasmasimulator.ethereum.models.ETHBlock
import io.plasmasimulator.ethereum.models.ETHChain
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

class EthereumManager : AbstractVerticle() {

  val chain = ETHChain()
  var deployedVerticleIds = mutableListOf<String>()

  private companion object {
      private val LOG = LoggerFactory.getLogger(EthereumManager::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("Ethereum Manager deployed!")
    startConsumers()
    deployVerticles(config())
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
    deployedVerticleIds.forEach{ id ->
      vertx.undeploy(id)
    }
    deployedVerticleIds.clear()
  }

  fun startConsumers() {
    vertx.eventBus().consumer<Any>(Address.READY_TO_MINE.name) { msg ->
      val block = Json.decodeValue(msg.body().toString(), ETHBlock::class.java)
      if(chain.containsBlock(block.number)) {
        msg.reply(Message.FAILURE.name)
      } else {
        chain.addBlock(block)
        msg.reply(Message.SUCCESS.name)
      }
    }
  }

  fun deployVerticles(config: JsonObject) {
    LOG.info("Config: $config")

    var opt = DeploymentOptions()
      .setWorker(true)
      .setConfig(config)
      .setInstances(config.getInteger("numberOfEthereumNodes", 1))

    vertx.deployVerticle("io.plasmasimulator.ethereum.verticles.ETHNodeVerticle", opt) { ar ->
      if(ar.failed()) {
        LOG.info(ar.cause().toString())
      }
      deployedVerticleIds.add(ar.result())
    }
  }
}
