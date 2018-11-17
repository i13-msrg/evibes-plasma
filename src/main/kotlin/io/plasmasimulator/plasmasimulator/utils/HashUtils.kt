package io.plasmasimulator.plasmasimulator.utils

import java.security.MessageDigest

object HashUtils {
  val HEX_CHARS = "0123456789abcdef"

  fun hash(bytesToHash: ByteArray) : ByteArray {
    val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
    val resultArray = digest.digest()
    return resultArray
  }

  fun hash(byteArrays: Array<ByteArray?>) : ByteArray{
    val digest: MessageDigest = MessageDigest.getInstance("SHA-256")

    for(byteArray in byteArrays) {
      digest.update(byteArray)
    }
    val resultArray = digest.digest()
    return resultArray
  }

  fun transform(byteArray: ByteArray) : String {
    val result = StringBuilder(byteArray.size * 2)

    byteArray.forEach {
      val i = it.toInt()
      result.append(HEX_CHARS[i shr 4 and 0x0f])
      result.append(HEX_CHARS[i and 0x0f])
    }

    return result.toString()
  }
}
