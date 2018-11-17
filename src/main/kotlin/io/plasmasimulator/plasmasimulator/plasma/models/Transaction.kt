package io.plasmasimulator.plasmasimulator.plasma.models

class Transaction {
  var inputs = mutableListOf<Input>()
  var outputs = mutableListOf<Output>()

  class Input(val blockNum: Int, val txIndex: Int, val outputIndex: Int, val sig: String = "")
  class Output(address: String, amount: Int)

  fun addInput(blockNum: Int, txIndex: Int, outputIndex: Int) {
    inputs.add(Input(blockNum, txIndex, outputIndex))
  }

  fun addOutput(address: String, amount: Int) {
    outputs.add(Output(address, amount))
  }

}
