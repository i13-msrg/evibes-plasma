package io.plasmasimulator.ethereum.contracts

import io.plasmasimulator.ethereum.models.Account
import io.plasmasimulator.utils.HashUtils
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

class PlasmaContract(val plasmaBlockInterval: Int) {
  var state: MutableMap<String, Account> = mutableMapOf()
  var plasmaBlockNumber = 0
  var depositBlockNumber = 1
  var childBlocks = mutableMapOf<Int, PlasmaBlock>()

  init {
      childBlocks.put(0, PlasmaBlock(HashUtils.transform(HashUtils.hash("0,-1".toByteArray()))))
      plasmaBlockNumber = plasmaBlockInterval
  }

  companion object {
      private val LOG = LoggerFactory.getLogger(PlasmaContract::class.java)
  }

  fun deposit(address: String, amount: Int, chainAddress: String) : JsonObject{
    LOG.info("Deposit $amount for $address")
    state.put(address, Account(0, address, amount))
    val rootHash = HashUtils.transform(HashUtils.hash(address.toByteArray() + amount.toByte()))
    childBlocks.put(depositBlockNumber, PlasmaBlock(rootHash))
    return JsonObject()
      .put("address", address)
      .put("amount", amount)
      .put("blockNum", depositBlockNumber++)
      .put("rootHash", rootHash)
      .put("chainAddress", chainAddress)
  }

  fun submitBlock(rootHash: String) {
    childBlocks.put(plasmaBlockNumber, PlasmaBlock(rootHash))
    updateBlockNumbers()
  }

  fun updateBlockNumbers() {
    plasmaBlockNumber += plasmaBlockInterval
    depositBlockNumber = plasmaBlockNumber + 1
  }
}

class PlasmaBlock(var rootHash: String = "") {
  val timestamp = System.currentTimeMillis()
}
