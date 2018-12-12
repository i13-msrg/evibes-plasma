package io.plasmasimulator.ethereum.models

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.vertx.core.json.JsonObject

class ETHTransaction(val nonce: Int, val from: String, val to: String, val amount: Int?, var data: Map<String, String>?)
