package io.plasmasimulator.ethereum.models

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.vertx.core.json.JsonObject

// nonce -> number of transactions sent by this sender
class ETHTransaction(val nonce: Int,
                     val from: String,
                     val to: String,
                     val amount: Int?,
                     val gasPrice: Int?,
                     val gasLimit: Int,
                     var data: Map<String, String>?,
                     val timestamp: Long = System.currentTimeMillis()) {

  override fun equals(other: Any?): Boolean {
    if(other == null) return false
    if(other !is ETHTransaction) return false

    if(nonce != other.nonce) return false
    if(from != other.from) return false
    if(to != other.to) return false
    if(amount != other.amount) return false
    if(timestamp != other.timestamp) return false

    return true
  }

  override fun hashCode(): Int {
    var result = 17
    result = 31 * result + nonce
    result = 31 * result + from.hashCode()
    result = 31 * result + to.hashCode()
    result = 31 * result + timestamp.hashCode()

    return result
  }
}
