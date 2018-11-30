package io.plasmasimulator.plasma.models

class UTXO(val blockNum: Int, val txIndex: Int, val index: Int, val fromContract: Boolean = false) {

  override fun equals(other: Any?): Boolean {
    if(other == null) return false
    if((!(other is UTXO))) return false

    if(blockNum != other.blockNum) return false
    if(txIndex != other.txIndex) return false
    if(index != other.index) return false
    if(fromContract != other.fromContract) return false

    return true
  }

  override fun hashCode(): Int {
    var result = 17
    result = 31 * result + blockNum
    result = 31 * result + txIndex
    result = 31 * result + index
    result = 31 * result + fromContract.hashCode()
    return result
  }
}
