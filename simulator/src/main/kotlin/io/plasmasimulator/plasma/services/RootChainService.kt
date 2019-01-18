package io.plasmasimulator.plasma.services

import io.plasmasimulator.conf.Address
import io.plasmasimulator.ethereum.models.ETHBlock
import io.plasmasimulator.ethereum.models.ETHChain
import io.plasmasimulator.ethereum.models.ETHTransaction
import io.plasmasimulator.ethereum.verticles.ETHBaseNode
import io.plasmasimulator.utils.HashUtils
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

// this class gives the opportunity to plasma participants to call methods of plasma (rootchain) contracts

open class RootChainService : ETHBaseNode() {
  val plasmaAddress = ""
  private var nonce = 1
  private var pendingTransactions = mutableListOf<ETHTransaction>()
  private var transactionGas = 0

  private companion object {
      private val LOG = LoggerFactory.getLogger(RootChainService::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("ROOT CHAIN ADDRESS $ethAddress")
    transactionGas = config().getInteger("transactionGas")
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }

  fun deposit(address: String, amount: Int, chainAddress: String, parentPlasmaAddress: String?) {
    var data = mutableMapOf<String, String>()
    data.put("type", "plasma")
    data.put("method", "deposit")
    data.put("address", address)
    data.put("amount", amount.toString())
    data.put("chainAddress", chainAddress)

    if(parentPlasmaAddress != null)
      data.put("parentPlasmaAddress", parentPlasmaAddress)

    val tx: ETHTransaction = createTransactionToPlasmaContract(address, data)
    sendTransaction(tx)
  }

  fun submitBlock(from: String, rootHash: ByteArray, timestamp: Long) {
    val data = mutableMapOf<String, String>()
    data.put("type", "plasma")
    data.put("rootHash", HashUtils.transform(rootHash))
    data.put("method", "submitBlock")
    data.put("timestamp", timestamp.toString())
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
    if(peers.size < 1) {
      LOG.info("NO PEERS")
      pendingTransactions.add(tx)
      return
    }
    //vertx!!.eventBus().publish(Address.ETH_SUBMIT_TRANSACTION.name, JsonObject(Json.encode(tx)))
    for(peer in peers) {
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
                            gasLimit = transactionGas,
                            gasPrice = 20)
  }

  private fun createTransaction(from: String, to: String, amount: Int) : ETHTransaction {
    return ETHTransaction(nonce = nonce++,
                          from = from,
                          to = to,
                          amount = amount,
                          data = null,
                          gasLimit = transactionGas,
                          gasPrice = 20)
  }

  override fun handlePropagateBlock(block: ETHBlock) {
    if(!ethChain.containsBlock(block.number)) {
      ethChain.addBlock(block)
      propagateBlock(block)
    }
//  else {
//    LOG.info("[$ethAddress]: attempted to add block ${block.number}, but it already exists!")
//  }
  }

  override fun handlePropagateTransaction(tx: ETHTransaction) {
    if(!txPool.contains(tx)) {
      txPool.add(tx)
      propagateTransaction(tx)
    }
  }

  override fun handlePropagateTransactions(txs: List<ETHTransaction>) {

  }

  override fun handleSetNewPeers(newPeers: JsonArray) {
    var peersEmpty = peers.size == 0
    peers.clear()
    newPeers.forEach { peer ->
      peers.add(peer.toString())
    }
    if(peersEmpty && peers.size > 0) {
      sendPendingTransactions()
    }
  }

  private fun sendPendingTransactions() {
    pendingTransactions.forEach { tx ->
      sendTransaction(tx)
    }
    pendingTransactions.clear()
  }
}
