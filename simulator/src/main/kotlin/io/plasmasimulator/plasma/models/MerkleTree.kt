package io.plasmasimulator.plasma.models

import io.plasmasimulator.utils.HashUtils

class MerkleTree(var digest: ByteArray = mutableListOf<Byte>().toByteArray()) {

  var leftChild: MerkleTree? = null
  var rightChild: MerkleTree? = null

  fun add(leftChild: MerkleTree, rightChild: MerkleTree?) {
    this.leftChild = leftChild
    this.rightChild = rightChild
    if(rightChild != null) {
      digest = HashUtils.hash(arrayOf(leftChild.digest, rightChild.digest))
    }
    else {
      digest = leftChild.digest
    }
  }

  fun toHexString() : String {
    return HashUtils.transform(digest)
  }
}
