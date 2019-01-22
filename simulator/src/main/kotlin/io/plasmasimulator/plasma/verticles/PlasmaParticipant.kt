package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.plasma.models.*
import io.plasmasimulator.plasma.services.MainChainConnector
import io.plasmasimulator.utils.HashUtils
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import io.plasmasimulator.conf.Address

open class PlasmaParticipant: MainChainConnector() {
  var chain: PlasmaChain = PlasmaChain(chainAddress = "", plasmaBlockInterval = 10)
  var plasmaPool: UTXOPool = UTXOPool()
  val rootChainService = this

  val address: String = PlasmaParticipant.addressNum++.toString()//UUID.randomUUID().toString()
  var balance: Int = 0
  var myUTXOs = mutableListOf<UTXO>()
  var spentUTXOs = mutableListOf<UTXO>()
  var pendingUTXOs = myUTXOs.toMutableList()

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaParticipant::class.java)
    private var addressNum = 0
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    // deploying rootChainVerticle

    LOG.info("Initialize PlasmaVerticle $address")
    val chainAddress: String            = config().getString("chainAddress")
    val plasmaBlockInterval        = config().getInteger("plasmaBlockInterval")

    chain = PlasmaChain(chainAddress = chainAddress, plasmaBlockInterval = plasmaBlockInterval)

    if(config().containsKey("parentPlasmaAddress")) {
      chain.parentChainAddress          = config().getString("parentPlasmaAddress")
    }
    bootstrapBlockchain()

    if(chain.parentChainAddress != null) {
      // I have a parent plasma chain, so I am expecting to see some blocks
      vertx.eventBus().consumer<Any>(chain.chainAddress) { msg ->
        val parentBlock: PlasmaBlock = Json.decodeValue(msg.body().toString(), PlasmaBlock::class.java)
        chain.addParentBlock(parentBlock)
        if(this is Operator) {
          vertx.eventBus().send(Address.PARENT_BLOCK_RECEIVED.name, JsonObject().put("chainAddress", chainAddress))
        }
      }
    }
  }

  fun bootstrapBlockchain() {
    val genesisBlock = PlasmaBlock(number = 0, prevBlockNum = -1)
    genesisBlock.merkleRoot = HashUtils.hash("0,0,-1".toByteArray())
    pendingUTXOs = myUTXOs.toMutableList()
    createUTXOsForBlock(genesisBlock)
    //chain.addBlock(genesisBlock, UTXOPool())
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

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }
}
