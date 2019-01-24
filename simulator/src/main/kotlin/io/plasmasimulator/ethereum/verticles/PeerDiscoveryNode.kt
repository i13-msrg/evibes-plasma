package io.plasmasimulator.ethereum.verticles

import io.plasmasimulator.conf.Address
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

class PeerDiscoveryNode: AbstractVerticle() {

  var numberOfETHNodes = 0
  var numberOfNeighbours = 0
  var neighboursUpdateInterval = 0
  var nodeAddresses = mutableListOf<String>()

  private companion object {
      private val LOG = LoggerFactory.getLogger(PeerDiscoveryNode::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("PeerDiscoveryNode is deployed")

    numberOfETHNodes = config().getInteger("numberOfEthereumNodes")
    numberOfNeighbours = config().getInteger("numberOfNeighbours")
    neighboursUpdateInterval = config().getInteger("neighboursUpdateInterval")
    startConsumers()
    startPeriodicUpdateOfNeighbours((neighboursUpdateInterval * 1000).toLong())
  }

  fun startPeriodicUpdateOfNeighbours(interval: Long) {
    vertx.setPeriodic(interval) { id ->
      LOG.info("Updating neighbours ...")
      distributeNewPeers()
    }
  }

  fun startConsumers() {
    vertx.eventBus().consumer<Any>(Address.ETH_PUBLISH_ADDRESS.name) { msg ->
      val newAddress: String = msg.body() as String
      if(!nodeAddresses.contains(newAddress)) {
        nodeAddresses.add(newAddress)
//        LOG.info("$newAddress arrived")
        distributeNewPeers()
      }
    }
  }

  fun distributeNewPeers() {
    for(address in nodeAddresses) {
      val peers = selectNewPeersFor(address)
      if(peers.size() > 0)
        sendNewPeers(address, peers)
    }
  }

  fun selectNewPeersFor(address: String): JsonArray {
    var allNodes = nodeAddresses.toMutableList()
    allNodes.remove(address)
    var neighbours = JsonArray()
    allNodes.shuffled().take(numberOfNeighbours).forEach { peer ->
      neighbours.add(peer)
    }
    return neighbours
  }

  fun sendNewPeers(address: String, peers: JsonArray) {
    val data = JsonObject().put("type", "setNewPeers")
                                 .put("peers", peers)
    vertx.eventBus().send(address, data)
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }
}
