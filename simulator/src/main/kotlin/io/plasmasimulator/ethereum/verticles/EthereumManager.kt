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
import kotlin.math.ceil

class EthereumManager : AbstractVerticle() {

  val chain = ETHChain()
  var deployedVerticleIds = mutableListOf<String>()
  var timerId: Long = 0
  var receivedBlocks = mutableMapOf<Int, MutableList<Long>>()
  var propagationTimes = mutableListOf<Long>()
  var propagationInfo = JsonObject()

  private companion object {
      private val LOG = LoggerFactory.getLogger(EthereumManager::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("Ethereum Manager deployed!")
    startConsumers(config())
    deployVerticles(config())
    val interval: Long  = (config().getInteger("externalTransactionGenerationRate") * 1000).toLong()
    val enableTransactionsGeneration = config().getBoolean("enableExternalTransactions")

    if(enableTransactionsGeneration) {
      startTXGeneration(interval)
    }
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
    vertx.cancelTimer(timerId)

    deployedVerticleIds.forEach{ id ->
      vertx.undeploy(id)
    }
    deployedVerticleIds.clear()
    LOG.info("Average propagation delay: ${averagePropagationDelay() / 1000}")
  }

  fun startConsumers(config: JsonObject) {
    vertx.eventBus().consumer<Any>(Address.READY_TO_MINE.name) { msg ->
      val block = Json.decodeValue(msg.body().toString(), ETHBlock::class.java)
      if(chain.containsBlock(block.number)) {
        msg.reply(Message.FAILURE.name)
      } else {
        chain.addBlock(block)
        msg.reply(Message.SUCCESS.name)
      }
    }
    val numberOfNodes = config.getInteger("numberOfEthereumNodes")

    vertx.eventBus().consumer<Any>(Address.ETH_BLOCK_RECEIVED.name) { msg ->
      var dataJson = msg.body() as JsonObject
      var blockNum = dataJson.getInteger("blockNumber")
      var timestamp = dataJson.getLong("timestamp")

      if(!receivedBlocks.containsKey(blockNum)){
        receivedBlocks.put(blockNum, mutableListOf())
      }
      var timestampList = receivedBlocks.get(blockNum)!!
      timestampList.add(timestamp)

      if(timestampList.size.toDouble() == ceil(0.25 * numberOfNodes)) {
        val sortedList = timestampList.sorted()
        val propagationDelay = sortedList[timestampList.size - 1] - sortedList[0]
        LOG.info("propagationDelay of block 25% $blockNum is $propagationDelay")
        propagationInfo.put("delay25", "%.2f".format(propagationDelay.toDouble() / 1000) )
      }

      if(timestampList.size.toDouble() == 0.50 * numberOfNodes) {
        val sortedList = timestampList.sorted()
        val propagationDelay = sortedList[timestampList.size - 1] - sortedList[0]
        LOG.info("propagationDelay of block 50% $blockNum is $propagationDelay")
        propagationInfo.put("delay50", "%.2f".format(propagationDelay.toDouble() / 1000) )
        //var data = JsonObject().put("blockNum", blockNum).put("delay", propagationDelay)
      }

      if(timestampList.size == numberOfNodes){
        val sortedList = timestampList.sorted()
        val propagationDelay = sortedList[timestampList.size - 1] - sortedList[0]

        propagationTimes.add(propagationDelay)
        LOG.info("propagationDelay of block $blockNum is $propagationDelay")
        propagationInfo.put("delay100", "%.2f".format(propagationDelay.toDouble() / 1000) )
        vertx.eventBus().send(Address.PROPAGATION_INFO.name, propagationInfo)
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
    val enableTransactionsGeneration = config.getBoolean("enableExternalTransactions")
    if(enableTransactionsGeneration) {
      vertx.deployVerticle("io.plasmasimulator.ethereum.verticles.TransactionManager",
        DeploymentOptions().setWorker(true).setConfig(config).setInstances(1)) { ar ->
        if(ar.failed()) {
          LOG.info(ar.cause().toString())
        }
        deployedVerticleIds.add(ar.result())
      }
    }

  }

  fun startTXGeneration(interval: Long) {
    vertx.setPeriodic(interval) { id ->
      timerId = id
      vertx.eventBus().send(Address.ETH_ISSUE_TRANSACTIONS.name, "")
    }
  }

  fun averagePropagationDelay() : Double {
    var size = propagationTimes.size
    var sum: Long = 0

    propagationTimes.forEach { propTime -> sum += propTime }

    return sum.toDouble() / size.toDouble()
  }
}
