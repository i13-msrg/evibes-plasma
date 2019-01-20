package io.plasmasimulator.conf

import io.vertx.config.ConfigRetrieverOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.ConfigStoreOptions

class Configuration {
  companion object {
    var configJSON = JsonObject()
      .put("numberOfEthereumNodes", 30)
      .put("numberOfPlasmaClients", 10)
      .put("tokensPerClient", 100)
      .put("transactionsPerPlasmaBlock", 5)
      .put("plasmaChildren", 0)
      .put("numberOfNeighbours", 20)
      .put("blockGasLimit", 300)
      .put("transactionGas", 30)
      .put("transactionGasPrice", 20)

      .put("enableExternalTransactions", true)
      .put("externalGasLimit", 10)
      .put("externalGasPrice", 20)
      .put("externalTransactionGenerationRate",  10)
      .put("numberOfEthereumExternalAccounts", 50)
      .put("amountPerEthereumExternalAccount", 100)

      .put("transactionGenerationRate", 5)
      .put("numberOfTransactionGenerationIntervals", 50)
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
