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
  }
}
