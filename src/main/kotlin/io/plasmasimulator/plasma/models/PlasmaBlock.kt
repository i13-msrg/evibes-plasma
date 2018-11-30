package io.plasmasimulator.plasma.models

import java.security.MessageDigest

class PlasmaBlock(val number: Int, val prevBlockNum: Int, val prevBlockHash: ByteArray, var transactions: List<Transaction> = mutableListOf<Transaction>()) {
  // TODO: signature
  var hash = mutableListOf<Byte>()
  var merkleRoot = mutableListOf<Byte>()
  val timestamp = System.currentTimeMillis()

  fun blockHash() : List<Byte> {
    if(hash.size > 0) return hash
    val digest = MessageDigest.getInstance("SHA-256")
    val blockHash = mutableListOf<Byte>()
    blockHash.add(number.toByte())
    blockHash.add(prevBlockNum.toByte())
    blockHash.addAll(prevBlockHash.toMutableList())

    for(tx in transactions) {
      blockHash.addAll(tx.txHashCode())
    }

    digest.update(blockHash.toByteArray())

    hash = digest.digest().toMutableList()

    return hash
  }
}
