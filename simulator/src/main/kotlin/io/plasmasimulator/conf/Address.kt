package io.plasmasimulator.conf

enum class Address {
  // Conf
  UPDATE_CONFIGURATION,
  GET_CONFIGURATION,
  // Web App
  START_SIMULATION,
  STOP_SIMULATION,
  ADD_NEW_MAIN_PLASMA_BLOCK,
  ADD_NEW_CHILD_PLASMA_BLOCK,
  SET_PLASMA_CHAIN_ADDRESSES,
  PLASMA_TRANSACTION_PUBLISHED,
  ADD_ETH_TRANSACTION,
  ADD_ETH_BLOCK,
  DEPOSIT_TRANSACTION_PUBLISHED,
  // ETH
  ETH_NODES_BROADCAST,
  ETH_DEPOSIT,
  ETH_SUBMIT_TRANSACTION,
  ETH_SUBMIT_BLOCK,
  ETH_ANNOUNCE_DEPOSIT,
  ETH_PUBLISH_ADDRESS,
  READY_TO_MINE,
  ETH_BLOCK_SUBMITTED,
  // Plasma
  ISSUE_TRANSACTION,
  RECEIVE_TRANSACTION,
  GENESIS_PLASMA_BLOCK,
  GENESIS_PLASMA_BLOCK_ADDED,
  PUBLISH_BLOCK,
  PUBLISH_BLOCK_CHILDREN,
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
