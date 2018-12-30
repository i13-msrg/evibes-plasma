package io.plasmasimulator.plasma.models

import java.util.*

class NestedChain(val address: String, val plasmaBlockInterval: Int) {
  var nextDepositBlockNumber = 1
  var nextPlasmaBlockNumber = plasmaBlockInterval
  var prevBlocksQueue = mutableMapOf<Int, LinkedList<NestedBlock>>()

  var blocks = mutableMapOf<Int, NestedBlock>()

  fun createBlock(number: Int, rootHash: String, timestamp: Long) {
    val block = NestedBlock(number = number, rootHash = rootHash, timestamp = timestamp)
  }

  fun addToPrevBlockQueue(block: NestedBlock) {
    if(!prevBlocksQueue.containsKey(nextDepositBlockNumber)) {
      prevBlocksQueue.put(nextDepositBlockNumber, LinkedList())
    }
    prevBlocksQueue.get(nextDepositBlockNumber)!!.push(block)
  }

  fun removeFromPrevBlockQueue() {
    if(!prevBlocksQueue.containsKey(nextDepositBlockNumber)) return
    val blocks = prevBlocksQueue.get(nextDepositBlockNumber)
    prevBlocksQueue.remove(nextDepositBlockNumber)

    blocks!!.forEach { block ->
      addBlock(block)
    }
  }

  fun updateNextDepositBlockNumber() {
    nextDepositBlockNumber = plasmaBlockInterval + 1
  }

  fun addBlock(block: NestedBlock) {
    // same block could be added more than once, since eth nodes submit deposit blocks
    // once they add them to the chain. This happens due to p2p characteristics
    if(blocks.containsKey(block.number)){
      return
    }

    if(nextDepositBlockNumber == block.number) {
      // we got the correct block
      // it could though happen that we previously received
      // children blocks of this block, which we added to a queue
      // so we need first to add such children blocks, if they exist
      if(prevBlocksQueue.containsKey(nextDepositBlockNumber)) {
        removeFromPrevBlockQueue()
      }
      blocks.put(block.number, block)

      nextDepositBlockNumber += 1
      return
    }

    if(nextPlasmaBlockNumber == block.number) {
      blocks.put(block.number, block)

      nextPlasmaBlockNumber += plasmaBlockInterval
      updateNextDepositBlockNumber()
      return
    }

    if(block.number > nextDepositBlockNumber) {
      // we receive deposit block from eth network that has a previous block(parent)
      // another deposit block that we are still waiting for to appear
      addToPrevBlockQueue(block)
      return
    }
  }
}

class NestedBlock(val number: Int, val rootHash: String, val timestamp: Long)
