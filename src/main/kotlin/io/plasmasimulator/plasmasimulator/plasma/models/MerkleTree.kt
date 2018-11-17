package io.plasmasimulator.plasmasimulator.plasma.models

import io.plasmasimulator.plasmasimulator.utils.HashUtils

class MerkleTree(var digest: ByteArray? = null) {

  var leftChild: MerkleTree? = null
  var rightChild: MerkleTree? = null

  fun add(leftChild: MerkleTree, rightChild: MerkleTree) {
    this.leftChild = leftChild
    this.rightChild = rightChild
    digest = HashUtils.hash(arrayOf(leftChild.digest, rightChild.digest))
  }

  fun toHexString() : String {
    return HashUtils.transform(digest)
  }
}
