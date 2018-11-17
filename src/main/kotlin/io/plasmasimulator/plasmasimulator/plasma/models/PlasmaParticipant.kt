package io.plasmasimulator.plasmasimulator.plasma.models

import io.plasmasimulator.plasmasimulator.plasma.models.PlasmaChain

interface PlasmaParticipant {
  val chain: PlasmaChain
  val address: String
  val plasmaContractAddress: String
}
