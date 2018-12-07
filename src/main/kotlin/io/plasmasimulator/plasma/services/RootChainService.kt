package io.plasmasimulator.plasma.services

import io.plasmasimulator.conf.Address
import io.plasmasimulator.ethereum.contracts.PlasmaContract
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

// this class gives the opportunity to plasma participants to call methods of plasma (rootchain) contracts

class RootChainService(val vertx: Vertx) {

  fun deposit(address: String, amount: Int) {
    val data = JsonObject().put("address", address).put("amount", amount)
    vertx.eventBus().publish(Address.ETH_DEPOSIT.name, data)
  }

  fun submitBlock(rootHash: ByteArray) {
    val data = JsonObject().put("rootHash", rootHash)
    vertx.eventBus().publish(Address.ETH_SUBMIT_BLOCK.name, data)
  }
}
