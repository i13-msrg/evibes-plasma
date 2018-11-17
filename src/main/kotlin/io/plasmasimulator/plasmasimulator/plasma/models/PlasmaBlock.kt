package io.plasmasimulator.plasmasimulator.plasma.models

class PlasmaBlock(val number: Int, val prevBlockNum: Int, prevBlockHash: String, var transactions: List<Transaction> = mutableListOf<Transaction>()) {
  // TODO: signature
  val timestamp = System.currentTimeMillis()

  fun getMerkleRoot() : String {
    return ""
  }
}
