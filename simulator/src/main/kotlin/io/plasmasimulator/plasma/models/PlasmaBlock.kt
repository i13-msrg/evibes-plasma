package io.plasmasimulator.plasma.models

import java.security.MessageDigest

class PlasmaBlock(val number: Int,
                  val prevBlockNum: Int,
                  var transactions: List<Transaction> = mutableListOf<Transaction>(),
                  var depositBlock: Boolean = false) {
  // TODO: signature
  var hash = ByteArray(0)
  var merkleRoot = ByteArray(0)
  val timestamp = System.currentTimeMillis()

  fun blockHash() : ByteArray {
    if(hash.size > 0) return hash
    val digest = MessageDigest.getInstance("SHA-256")
    val blockHash = mutableListOf<Byte>()
    blockHash.add(number.toByte())
    blockHash.add(prevBlockNum.toByte())

    for(tx in transactions) {
      blockHash + tx.txHashCode()
    }

    digest.update(blockHash.toByteArray())

    hash = digest.digest()

    return hash
  }
}
