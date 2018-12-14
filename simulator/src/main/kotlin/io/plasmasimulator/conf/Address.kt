package io.plasmasimulator.conf

enum class Address {
  // Conf
  UPDATE_CONFIGURATION,
  GET_CONFIGURATION,
  // Web App
  START_SIMULATION,
  STOP_SIMULATION,
  ADD_NEW_BLOCK,
  // ETH
  ETH_NODES_BROADCAST,
  ETH_DEPOSIT,
  ETH_SUBMIT_TRANSACTION,
  ETH_SUBMIT_BLOCK,
  ETH_ANNOUNCE_DEPOSIT,
  // Plasma
  ISSUE_TRANSACTION,
  RECEIVE_TRANSACTION,
  GENESIS_PLASMA_BLOCK,
  GENESIS_PLASMA_BLOCK_ADDED,
  PUBLISH_BLOCK,
  PUBLISH_TRANSACTION,
  PUBLISH_ADDRESS,
  PUSH_ALL_ADDRESSES,
  RECEIVED_ALL_ADDRESSES,
  APPLY_BLOCK,
  RUN_PLASMA_CHAIN,
  SET_PLASMA_CONTRACT_ADDRESS,
  REQUEST_ADDRESS,
  DEPOSIT_TRANSACTION,
  PUBLISH_BALANCE,

  PRINT_BALANCE_FOR_EACH_CLIENT
}
