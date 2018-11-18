package io.plasmasimulator.utils

import io.plasmasimulator.plasma.models.MerkleTree
import io.plasmasimulator.plasma.models.Transaction
import java.util.*

object MerkleTreeBuilder {
  fun getRoot(transactions: List<Transaction>) : MerkleTree {
    val leafs = createLeafs(transactions)
    val queue = LinkedList<MerkleTree>()

    for(leaf in leafs) {
      queue.push(leaf)
    }

    while(queue.isNotEmpty()) {
      val tree1 = queue.pop()
      // tree1 could be the last merkletree in the queue which makes it the root
      if(queue.isEmpty())
        return tree1

      val tree2 = queue.pop()
      val joinedMerkleTree = MerkleTree()
      joinedMerkleTree.add(tree1, tree2)
      queue.push(joinedMerkleTree)
    }

    return MerkleTree()
  }

  private fun createLeafs(transactions: List<Transaction>) : List<MerkleTree> {
    val leafs = mutableListOf<MerkleTree>()
    for(i in 0 until transactions.size) {
      leafs.add(MerkleTree(transactions[i].hash.toByteArray()))
    }
    // if the number of transactions is odd then the last hash must be duplicated
    // to create even number of transactions
    if(transactions.size % 2 != 0)
      leafs.add(MerkleTree(transactions.get(transactions.size - 1).hash.toByteArray()))

    return leafs
  }
}
