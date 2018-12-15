package io.plasmasimulator.ethereum.models

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.vertx.core.json.JsonObject

// nonce -> number of transactions sent by this sender
class ETHTransaction(val nonce: Int,
                     val from: String,
                     val to: String,
                     val amount: Int?,
                     //val gasPrice: Int?,
                     //val gasLimit: Int?,
                     var data: Map<String, String>?)
