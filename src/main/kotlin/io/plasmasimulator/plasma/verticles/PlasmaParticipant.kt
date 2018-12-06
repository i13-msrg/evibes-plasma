package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.SimulationManagerVerticle
import io.plasmasimulator.conf.Address
import io.plasmasimulator.plasma.models.PlasmaChain
import io.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasma.models.UTXO
import io.plasmasimulator.plasma.models.UTXOPool
import io.plasmasimulator.utils.FileManager
import io.plasmasimulator.utils.HashUtils
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.parsetools.JsonParser
import org.slf4j.LoggerFactory
import java.util.*

open class PlasmaParticipant: AbstractVerticle() {
  var chain: PlasmaChain = PlasmaChain()
  var plasmaPool: UTXOPool = UTXOPool()
  val address: String = PlasmaParticipant.addressNum++.toString()//UUID.randomUUID().toString()
  var balance: Int = 0
  var plasmaContractAddress = ""
  var myUTXOs = mutableListOf<UTXO>()
  var spentUTXOs = mutableListOf<UTXO>()
  var myFlyingUTXOS = myUTXOs.toMutableList()

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaParticipant::class.java)
    private var addressNum = 0
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    chain.vertx = vertx
    LOG.info("Initialize PlasmaVerticle")
    val jsonObj = config()
    plasmaContractAddress = jsonObj.getString("plasmaContractAddress")
    balance = jsonObj.getInteger("balance")

//    vertx.eventBus().consumer<Any>(Address.SET_PLASMA_CONTRACT_ADDRESS.name) { msg ->
//      LOG.info("Initialize PlasmaVerticle")
//      val jsonObj = msg.body() as JsonObject
//      plasmaContractAddress = jsonObj.getString("plasmaContractAddress")
//      balance = jsonObj.getInteger("balance")
//    }
    vertx.eventBus().consumer<Any>(Address.GENESIS_PLASMA_BLOCK.name) { msg ->
      LOG.info(msg.body().toString())
      val block: PlasmaBlock = Json.decodeValue(msg.body().toString(), PlasmaBlock::class.java)
      createUTXOsForBlock(block)
      myFlyingUTXOS = myUTXOs.toMutableList()
      chain.addBlock(block, plasmaPool)
      LOG.info(HashUtils.transform(block.blockHash().toByteArray()))
      if(this is Operator) {
        vertx.eventBus().send(Address.GENESIS_PLASMA_BLOCK_ADDED.name, "genesis block added")
      }
    }
  }

  fun removeUTXOsForBlock(block: PlasmaBlock) {
    for(tx in block.transactions) {
      for(input in tx.inputs) {
        val utxoToRemove = UTXO(input.blockNum, input.txIndex, input.outputIndex)
        if(myUTXOs.contains(utxoToRemove)) {
          myUTXOs.remove(utxoToRemove)
        }
        plasmaPool.removeUTXO(utxoToRemove)
      }
    }
  }

  fun createUTXOsForBlock(block: PlasmaBlock) {
    for((txIndex, tx) in block.transactions.withIndex()) {
      for((outputIndex, output) in tx.outputs.withIndex()) {
        val newUTXO = UTXO(block.number, txIndex, outputIndex)
        if(address == output.address && !myUTXOs.contains(newUTXO)){
          myUTXOs.add(newUTXO)
        }
        plasmaPool.addUTXO(newUTXO, output)
      }
    }
  }
}
