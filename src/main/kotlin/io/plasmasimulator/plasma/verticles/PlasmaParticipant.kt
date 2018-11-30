package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.SimulationManagerVerticle
import io.plasmasimulator.conf.Address
import io.plasmasimulator.plasma.models.PlasmaChain
import io.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasma.models.UTXO
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
  val address: String = UUID.randomUUID().toString()
  var balance: Int = 0
  var plasmaContractAddress = ""
  var myUTXOs = mutableListOf<UTXO>()

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaParticipant::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
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
      println(msg.body().toString())
      val block: PlasmaBlock = Json.decodeValue(msg.body().toString(), PlasmaBlock::class.java)
      createUTXOsForBlock(block)
      chain.addBlock(block)
      println(HashUtils.transform(block.blockHash().toByteArray()))
      if(this is Operator) {
        vertx.eventBus().send(Address.GENESIS_PLASMA_BLOCK_ADDED.name, "genesis block added")
        println("I AM OPERATOR")
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
        chain.plasmaPool.removeUTXO(utxoToRemove)
      }
    }
  }

  fun createUTXOsForBlock(block: PlasmaBlock) {
    for((txIndex, tx) in block.transactions.withIndex()) {
      for((outputIndex, output) in tx.outputs.withIndex()) {
        val newUTXO = UTXO(block.number, txIndex, outputIndex)
        println("coming address is ${output.address}")
        println("my address is ${address}")
        if(address == output.address)
          myUTXOs.add(newUTXO)
        chain.plasmaPool.addUTXO(newUTXO, output)
      }
    }
  }
}
