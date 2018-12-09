package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.SimulationManagerVerticle
import io.plasmasimulator.conf.Address
import io.plasmasimulator.ethereum.models.ETHChain
import io.plasmasimulator.plasma.models.*
import io.plasmasimulator.plasma.services.RootChainService
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
  val chain: PlasmaChain = PlasmaChain()
  val rootChainService = RootChainService()
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
    // TODO: Improve vetx referencing
    rootChainService.vertx = vertx
    LOG.info("Initialize PlasmaVerticle")
    val jsonObj = config()
    plasmaContractAddress = jsonObj.getString("plasmaContractAddress")
    bootstrapBlockchain()
  }

  fun bootstrapBlockchain() {
    val genesisBlock = PlasmaBlock(number = 0, prevBlockNum = 0, prevBlockHash = mutableListOf<Byte>().toByteArray())
    genesisBlock.merkleRoot = HashUtils.hash("0,0,-1".toByteArray())
    myFlyingUTXOS = myUTXOs.toMutableList()
    createUTXOsForBlock(genesisBlock)
    chain.addBlock(genesisBlock, UTXOPool())
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
