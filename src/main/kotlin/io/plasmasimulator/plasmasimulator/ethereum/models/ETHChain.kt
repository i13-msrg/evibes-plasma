package io.plasmasimulator.plasmasimulator.ethereum.models

import io.plasmasimulator.plasmasimulator.ethereum.models.ETHBlock
import io.plasmasimulator.plasmasimulator.ethereum.models.ETHTransaction

class ETHChain {
  var blocks = mutableMapOf<Int, ETHBlock>()

  fun addBlock(block: ETHBlock) {
    this.blocks.put(block.number, block)
  }

  fun validateBlock(block: ETHBlock): Boolean {
    // TODO
    return true
  }

  fun validateTransaction(tx: ETHTransaction): Boolean {
    // TODO
    return true
  }

  fun getBlock(number: Int) {
    blocks.get(number)
  }

}
