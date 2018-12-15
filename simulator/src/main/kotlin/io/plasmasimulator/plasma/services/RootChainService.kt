package io.plasmasimulator.plasma.services

import io.plasmasimulator.conf.Address
import io.plasmasimulator.ethereum.contracts.PlasmaContract
import io.plasmasimulator.ethereum.models.ETHTransaction
import io.plasmasimulator.utils.HashUtils
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject

// this class gives the opportunity to plasma participants to call methods of plasma (rootchain) contracts

class RootChainService() {
  var vertx: Vertx? = null
  val plasmaAddress = ""
  private var nonce = 1

  fun deposit(address: String, amount: Int, chainAddress: String) {
    var data = mutableMapOf<String, String>()
    data.put("type", "plasma")
    data.put("method", "deposit")
    data.put("address", address)
    data.put("amount", amount.toString())
    data.put("chainAddress", chainAddress)

    val tx: ETHTransaction = createTransaction(address, data)
    val txJson = Json.encode(tx)

    if(vertx != null)
      vertx!!.eventBus().publish(Address.ETH_SUBMIT_TRANSACTION.name, txJson)
  }

  fun submitBlock(from: String, rootHash: ByteArray) {
    var data = mutableMapOf<String, String>()
    data.put("type", "plasma")
    data.put("rootHash", HashUtils.transform(rootHash))
    data.put("method", "submitBlock")
    //val data = "rootHash:${HashUtils.transform(rootHash)}"
    //val data = JsonObject().put("type", "plasma").put("rootHash", rootHash).put("method", "submitBlock")
    val tx: ETHTransaction = createTransaction(from, data)
    val txJson = Json.encode(tx)
    println(txJson)
    if(vertx != null)
      vertx!!.eventBus().publish(Address.ETH_SUBMIT_TRANSACTION.name, txJson)
  }

  private fun createTransaction(from: String, data: Map<String, String>) : ETHTransaction {
      return ETHTransaction(nonce = nonce++, from = from, to = plasmaAddress, amount = null, data = data)
  }
}
