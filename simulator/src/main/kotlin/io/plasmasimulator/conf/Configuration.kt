package io.plasmasimulator.conf

import io.vertx.config.ConfigRetrieverOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.ConfigStoreOptions

class Configuration {
  companion object {
    var configJSON = JsonObject()
      .put("numberOfEthereumNodes", 5)
      .put("numberOfPlasmaClients", 4)
      .put("tokensPerClient", 100)
      .put("transactionsPerPlasmaBlock", 2)
      .put("plasmaChildren", 0)
      .put("numberOfPeers", 8)
      .put("blockGasLimit", 120)
      .put("transactionGas", 60)
      .put("transactionGenerationRate", 10)
      .put("numberOfTransactionGenerationIntervals", 10)
      .put("difficulty", 131072)
      .put("plasmaBlockInterval", 100)

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
