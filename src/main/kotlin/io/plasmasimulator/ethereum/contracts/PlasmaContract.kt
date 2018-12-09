package io.plasmasimulator.ethereum.contracts

import io.plasmasimulator.ethereum.models.Account
import io.plasmasimulator.utils.HashUtils
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

class PlasmaContract {
  var state: MutableMap<String, Account> = mutableMapOf()
  var childBlocks = mutableListOf<PlasmaBlock>()

  init {
      childBlocks.add(PlasmaBlock(HashUtils.transform(HashUtils.hash("0,0,-1".toByteArray()))))
  }

  companion object {
      private val LOG = LoggerFactory.getLogger(PlasmaContract::class.java)
  }

  fun deposit(address: String, amount: Int) : JsonObject{
    state.put(address, Account(address, amount))
    val rootHash = HashUtils.transform(HashUtils.hash(address.toByteArray() + amount.toByte()))
    childBlocks.add(PlasmaBlock(rootHash))
    return JsonObject()
      .put("address", address)
      .put("amount", amount)
      .put("blockNum", childBlocks.size - 1)
      .put("rootHash", rootHash)
  }

  fun submitBlock(rootHash: String) {
    childBlocks.add(PlasmaBlock(rootHash))
  }
}

class PlasmaBlock(var rootHash: String = "") {
  val timestamp = System.currentTimeMillis()
}
