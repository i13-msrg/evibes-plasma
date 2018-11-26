package io.plasmasimulator.plasma.models

import io.plasmasimulator.utils.HashUtils

class MerkleTree(var digest: ByteArray = mutableListOf<Byte>().toByteArray()) {

  var leftChild: MerkleTree = MerkleTree()
  var rightChild: MerkleTree? = MerkleTree()

  fun add(leftChild: MerkleTree, rightChild: MerkleTree) {
    this.leftChild = leftChild
    this.rightChild = rightChild
    digest = HashUtils.hash(arrayOf(leftChild.digest, rightChild.digest))
  }

  fun toHexString() : String {
    return HashUtils.transform(digest)
  }
}
