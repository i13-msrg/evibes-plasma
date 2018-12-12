package io.plasmasimulator.conf

import io.vertx.config.ConfigRetrieverOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.ConfigStoreOptions

class Configuration {
  companion object {
    var configJSON = JsonObject()
      .put("numberOfEthereumNodes", 3)
      .put("numberOfPlasmaClients", 6)
      .put("tokensPerClient", 10)

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