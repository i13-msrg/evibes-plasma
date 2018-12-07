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
    val rootHash = HashUtils.hash(address.toByteArray() + amount.toByte())
    childBlocks.add(PlasmaBlock(rootHash))
    return JsonObject()
      .put("address", address)
      .put("amount", amount)
      .put("rootHash", rootHash)
  }

  fun submit_block(rootHash: ByteArray) {
    childBlocks.add(PlasmaBlock(rootHash))
  }
}

class PlasmaBlock(var rootHash: ByteArray = ByteArray(0)) {
  var timestamp = System.currentTimeMillis()
}
