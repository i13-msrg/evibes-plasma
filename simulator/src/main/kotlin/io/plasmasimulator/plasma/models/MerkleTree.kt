package io.plasmasimulator.plasma.models

import io.plasmasimulator.utils.HashUtils

class MerkleTree(var digest: ByteArray = mutableListOf<Byte>().toByteArray()) {

  var leftChild: MerkleTree? = null
  var rightChild: MerkleTree? = null

  fun add(leftChild: MerkleTree, rightChild: MerkleTree?) {
    this.leftChild = leftChild
    this.rightChild = rightChild
    digest = if(rightChild != null) {
      HashUtils.hash(arrayOf(leftChild.digest, rightChild.digest))
    } else {
      leftChild.digest
    }
  }

  fun toHexString() : String {
    return HashUtils.transform(digest)
  }
}
