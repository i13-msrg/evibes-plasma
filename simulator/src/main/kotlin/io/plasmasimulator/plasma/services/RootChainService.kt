package io.plasmasimulator.plasma.services

import io.plasmasimulator.conf.Address
import io.plasmasimulator.ethereum.models.ETHBlock
import io.plasmasimulator.ethereum.models.ETHChain
import io.plasmasimulator.ethereum.models.ETHTransaction
import io.plasmasimulator.utils.HashUtils
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

// this class gives the opportunity to plasma participants to call methods of plasma (rootchain) contracts

class RootChainService(val vertx: Vertx, val ethAddress: String, val ethChain: ETHChain) {
  val plasmaAddress = ""
  private var nonce = 1
  var peers = mutableListOf<String>()

  init {
      startConsumers()
  }

  private companion object {
      private val LOG = LoggerFactory.getLogger(RootChainService::class.java)
  }

  fun deposit(address: String, amount: Int, chainAddress: String) {
    var data = mutableMapOf<String, String>()
    data.put("type", "plasma")
    data.put("method", "deposit")
    data.put("address", address)
    data.put("amount", amount.toString())
    data.put("chainAddress", chainAddress)

    val tx: ETHTransaction = createTransactionToPlasmaContract(address, data)
    sendTransaction(tx)
  }

  fun submitBlock(from: String, rootHash: ByteArray) {
    val data = mutableMapOf<String, String>()
    data.put("type", "plasma")
    data.put("rootHash", HashUtils.transform(rootHash))
    data.put("method", "submitBlock")
    //val data = "rootHash:${HashUtils.transform(rootHash)}"
    //val data = JsonObject().put("type", "plasma").put("rootHash", rootHash).put("method", "submitBlock")
    val tx: ETHTransaction = createTransactionToPlasmaContract(from, data)
    sendTransaction(tx)
  }

  fun sendTokens(from: String, to: String, amount: Int) {
    val tx = createTransaction(from, to, amount)
    sendTransaction(tx)
  }

  private fun sendTransaction(tx: ETHTransaction) {
    if(vertx == null) {
      LOG.info("vertx is null")
      return
    }
    if(peers.size < 1) {
      LOG.info("NO PEERS")
    }
    //vertx!!.eventBus().publish(Address.ETH_SUBMIT_TRANSACTION.name, JsonObject(Json.encode(tx)))
    for(peer in peers) {
      LOG.info("sending to peer $peer")
      val data = JsonObject()
        .put("type", "propagateTransaction")
        .put("transaction", JsonObject(Json.encode(tx)))

      vertx.eventBus().send(peer, data)
    }
  }

  private fun createTransactionToPlasmaContract(from: String, data: Map<String, String>) : ETHTransaction {
      return ETHTransaction(nonce = nonce++,
                            from = from,
                            to = plasmaAddress,
                            amount = null,
                            data = data,
                            gasLimit = 30,
                            gasPrice = 20)
  }

  private fun createTransaction(from: String, to: String, amount: Int) : ETHTransaction {
    return ETHTransaction(nonce = nonce++,
                          from = from,
                          to = to,
                          amount = amount,
                          data = null,
                          gasLimit = 30,
                          gasPrice = 20)
  }

  fun startConsumers() {
    vertx.eventBus().consumer<Any>(ethAddress) { msg ->
      val jsonObject = msg.body() as JsonObject
      when(jsonObject.getString("type")) {
        "propagateBlock" -> { // syncing with ethereum, I am client, not a node
          val blockJson = jsonObject.getJsonObject("block")
          val block: ETHBlock = Json.decodeValue(blockJson.toString(), ETHBlock::class.java)
          if(ethChain.containsBlock(block.number)) {
            LOG.info("[$ethAddress]: attempted to add block ${block.number}, but it already exists!")
          } else {
            ethChain.addBlock(block)
            LOG.info("[$ethAddress]: added block ${block.number}")
          }
        }

        "setNewPeers" -> {
          LOG.info("[Plasma $ethAddress received setNewPeers]")
          peers.clear()
          jsonObject.getJsonArray("peers").forEach { peer ->
            peers.add(peer.toString())
          }
        }
      }

    }
  }
}
