package io.plasmasimulator.conf

import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.web.handler.sockjs.BridgeOptions

object BridgeOptionsConfig {
  fun getOptions() : BridgeOptions {
    return BridgeOptions()
      .addInboundPermitted(PermittedOptions().setAddress(Address.UPDATE_CONFIGURATION.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.UPDATE_CONFIGURATION.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.GET_CONFIGURATION.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.GET_CONFIGURATION.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.START_SIMULATION.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.START_SIMULATION.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.STOP_SIMULATION.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.STOP_SIMULATION.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.ADD_NEW_MAIN_PLASMA_BLOCK.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.ADD_NEW_MAIN_PLASMA_BLOCK.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.SET_PLASMA_CHAIN_ADDRESSES.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.SET_PLASMA_CHAIN_ADDRESSES.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.ADD_NEW_CHILD_PLASMA_BLOCK.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.ADD_NEW_CHILD_PLASMA_BLOCK.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.PLASMA_TRANSACTION_PUBLISHED.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.PLASMA_TRANSACTION_PUBLISHED.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.DEPOSIT_TRANSACTION_PUBLISHED.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.DEPOSIT_TRANSACTION_PUBLISHED.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.ADD_ETH_TRANSACTION.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.ADD_ETH_TRANSACTION.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.ADD_ETH_BLOCK.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.ADD_ETH_BLOCK.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.NUMBER_OF_UTXOS.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.NUMBER_OF_UTXOS.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.PARENT_BLOCK_RECEIVED.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.PARENT_BLOCK_RECEIVED.name))
      .addInboundPermitted(PermittedOptions().setAddress(Address.PROPAGATION_INFO.name))
      .addOutboundPermitted(PermittedOptions().setAddress(Address.PROPAGATION_INFO.name))
  }
}
