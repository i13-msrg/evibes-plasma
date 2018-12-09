package io.plasmasimulator.conf

import io.vertx.config.ConfigRetrieverOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.ConfigRetrieverOptions
import io.vertx.kotlin.config.ConfigStoreOptions

class Configuration {
  companion object {
    val jsonObject = JsonObject()
      .put("instances", 3)
      .put("numberOfPlasmaClients", 6)
      .put("amountPerClient", 10)

    fun getConfigRetrieverOptions() : ConfigRetrieverOptions {
      val jsonStore = ConfigStoreOptions()
        .setType("json")
        .setConfig(jsonObject)
      return ConfigRetrieverOptions().addStore(jsonStore)
    }
  }
}
