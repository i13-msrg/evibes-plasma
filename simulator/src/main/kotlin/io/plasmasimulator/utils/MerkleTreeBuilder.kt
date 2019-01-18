package io.plasmasimulator.utils

import io.plasmasimulator.plasma.models.MerkleTree
import io.plasmasimulator.plasma.models.Transaction
import java.util.*

object MerkleTreeBuilder {
  fun getRoot(transactions: MutableList<Transaction>) : MerkleTree {
    val leafs = createLeafs(transactions)
    return buildTree(leafs)
  }

  fun buildTree(trees: MutableList<MerkleTree>): MerkleTree {
    if(trees.size == 1) return trees[0]
    var parents = mutableListOf<MerkleTree>()

    for(i in 0 until trees.size step 2) {
      val parentTree = MerkleTree()
      val right = if(trees.size > i + 1) trees[i+1] else null
      parentTree.add(trees[i], right)
      parents.add(parentTree)
    }
    return buildTree(parents)
  }

  private fun createLeafs(transactions: MutableList<Transaction>) : MutableList<MerkleTree> {
    val leafs = mutableListOf<MerkleTree>()
    for(i in 0 until transactions.size) {
      leafs.add(MerkleTree(transactions[i].txHashCode()))
    }
    // if the number of transactions is odd then the last hash must be duplicated
    // to create even number of transactions
    if(transactions.size % 2 != 0)
      leafs.add(MerkleTree(transactions.get(transactions.size - 1).txHashCode()))

    return leafs
  }
}
