package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.SimulationManagerVerticle
import io.plasmasimulator.conf.Address
import io.plasmasimulator.plasma.models.PlasmaChain
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.util.*

open class PlasmaParticipant: AbstractVerticle() {
  var chain: PlasmaChain = PlasmaChain()
  val address: String = UUID.randomUUID().toString()
  var balance: Int = 0
  var plasmaContractAddress = ""

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaParticipant::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("Initialize PlasmaVerticle")
    val jsonObj = config()
    plasmaContractAddress = jsonObj.getString("plasmaContractAddress")
    balance = jsonObj.getInteger("balance")

//    vertx.eventBus().consumer<Any>(Address.SET_PLASMA_CONTRACT_ADDRESS.name) { msg ->
//      LOG.info("Initialize PlasmaVerticle")
//      val jsonObj = msg.body() as JsonObject
//      plasmaContractAddress = jsonObj.getString("plasmaContractAddress")
//      balance = jsonObj.getInteger("balance")
//    }
  }
}
