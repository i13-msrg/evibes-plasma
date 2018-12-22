package io.plasmasimulator.ethereum.models

import io.plasmasimulator.ethereum.models.ETHBlock
import io.plasmasimulator.ethereum.models.ETHTransaction

class ETHChain {
  var blocks = mutableMapOf<Int, ETHBlock>()

  init {
    val genesisBlock = ETHBlock(0,-1, mutableListOf())
    this.addBlock(genesisBlock)
  }

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

  fun containsBlock(blockNum: Int) : Boolean {
    return blocks.containsKey(blockNum)
  }

  fun getLastBlock(): ETHBlock? {
    return blocks.get(blocks.keys.sorted().last())
  }

}
