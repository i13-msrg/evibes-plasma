package io.plasmasimulator.conf

import io.vertx.config.ConfigRetrieverOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.ConfigStoreOptions

class Configuration {
  companion object {
    var configJSON = JsonObject()
      .put("numberOfEthereumNodes", 10)
      .put("numberOfPlasmaClients", 6)
      .put("tokensPerClient", 10)
      .put("transactionsPerPlasmaBlock", 3)
      .put("plasmaChildren", 0)
      .put("numberOfPeers", 8)
      .put("blockGasLimit", 180)
      .put("difficulty", 131072)

    fun getConfigRetrieverOptions() : ConfigRetrieverOptions {
      val jsonStore = ConfigStoreOptions()
        .setType("json")
        .setConfig(configJSON)
      return ConfigRetrieverOptions().addStore(jsonStore)
    }

    fun setConfigRetrieverOptions(newJsonObject: JsonObject) {
      configJSON = newJsonObject
    }
  }
}
