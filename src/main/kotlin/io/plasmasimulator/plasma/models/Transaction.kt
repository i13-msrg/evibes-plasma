package io.plasmasimulator.plasma.models

import java.security.MessageDigest

class Transaction {
  val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
  var hash = mutableListOf<Byte>()
  var inputs = mutableListOf<Input>()
  var outputs = mutableListOf<Output>()

  class Input(val blockNum: Int, val txIndex: Int, val outputIndex: Int, val sig: String = "")
  class Output(val address: String, val amount: Int)

  fun addInput(blockNum: Int, txIndex: Int, outputIndex: Int) {
    inputs.add(Input(blockNum, txIndex, outputIndex))
  }

  fun addOutput(address: String, amount: Int) {
    outputs.add(Output(address, amount))
  }

  fun getTxHash() : List<Byte> {
    if(hash.size > 0) return hash

    var tx = mutableListOf<Byte>()

    for(input in inputs) {
      tx.add(input.blockNum.toByte())
      tx.add(input.txIndex.toByte())
      tx.add(input.outputIndex.toByte())
    }
    for(output in outputs) {
      tx.add(output.amount.toByte())
      tx.add(output.address.toByte())
    }
    digest.update(tx.toByteArray())

    hash = digest.digest().toMutableList()

    return hash
  }

}