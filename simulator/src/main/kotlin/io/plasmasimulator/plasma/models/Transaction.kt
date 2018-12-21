package io.plasmasimulator.plasma.models

import java.security.MessageDigest

class Transaction {
  var source = "-1"
  var hash = ByteArray(0)
  var inputs = mutableListOf<Input>()
  var outputs = mutableListOf<Output>()
  var depositTransaction = false
  var childChainTransaction = false
  var childChainData = mutableMapOf<String, String>() // block number, block merkle root come in here
  val timestamp = System.currentTimeMillis()

  class Input(val blockNum: Int, val txIndex: Int, val outputIndex: Int, val sig: String = "")
  class Output(val address: String, val amount: Int)

  fun addInput(blockNum: Int, txIndex: Int, outputIndex: Int) {
    inputs.add(Input(blockNum, txIndex, outputIndex))
  }

  fun addOutput(address: String, amount: Int) {
    outputs.add(Output(address, amount))
  }

  fun txHashCode() : ByteArray {
    if(hash.size > 0) return hash
    val digest: MessageDigest = MessageDigest.getInstance("SHA-256")

    var tx = mutableListOf<Byte>()

    for(input in inputs) {
      tx.add(input.blockNum.toByte())
      tx.add(input.txIndex.toByte())
      tx.add(input.outputIndex.toByte())
    }
    for(output in outputs) {
      tx.add(output.amount.toByte())
      tx.addAll(output.address.toByteArray().toMutableList())
    }

    if(childChainTransaction) {
      childChainData.forEach { key: String, value: String ->
        tx.addAll(value.toByteArray().toMutableList())
      }
    }

    digest.update(tx.toByteArray())

    hash = digest.digest()

    return hash
  }

}
