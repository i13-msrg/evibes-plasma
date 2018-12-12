package io.plasmasimulator.plasma.models

class UTXO(val blockNum: Int, val txIndex: Int, val index: Int) {

  override fun equals(other: Any?): Boolean {
    if(other == null) return false
    if(other !is UTXO) return false

    if(blockNum != other.blockNum) return false
    if(txIndex != other.txIndex) return false
    if(index != other.index) return false

    return true
  }

  override fun hashCode(): Int {
    var result = 17
    result = 31 * result + blockNum
    result = 31 * result + txIndex
    result = 31 * result + index
    return result
  }
}
