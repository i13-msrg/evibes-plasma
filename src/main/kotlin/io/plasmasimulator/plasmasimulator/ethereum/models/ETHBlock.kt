package io.plasmasimulator.plasmasimulator.ethereum.models

class ETHBlock(val number: Int, val prevBlockHash: String) {
  val timestamp = System.currentTimeMillis()
  var transactions = setOf<ETHTransaction>()
  var hash = this.hashCode()
}
