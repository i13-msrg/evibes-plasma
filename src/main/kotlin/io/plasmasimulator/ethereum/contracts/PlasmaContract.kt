package io.plasmasimulator.ethereum.contracts

import io.plasmasimulator.ethereum.models.Account
import io.plasmasimulator.utils.HashUtils
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject

class PlasmaContract {
  var state: MutableMap<String, Account> = mutableMapOf<String, Account>()
  var childBlocks = mutableListOf<PlasmaBlock>()

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
    println("BLOCK ADDED TO PLASMA CONTRACT")
    childBlocks.add(PlasmaBlock(rootHash))
  }
}

class PlasmaBlock(var rootHash: String = "") {
  var timestamp = System.currentTimeMillis()
}
