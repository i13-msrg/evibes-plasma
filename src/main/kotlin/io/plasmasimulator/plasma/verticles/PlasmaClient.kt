package io.plasmasimulator.plasma.verticles

import io.plasmasimulator.SimulationManagerVerticle
import io.plasmasimulator.conf.Address
import io.plasmasimulator.conf.Message
import io.plasmasimulator.plasma.models.PlasmaBlock
import io.plasmasimulator.plasma.models.PlasmaChain
import io.plasmasimulator.plasma.models.Transaction
import io.plasmasimulator.plasma.models.UTXO
import io.plasmasimulator.utils.HashUtils
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.util.*

class PlasmaClient: PlasmaParticipant() {

  var allOtherClientsAddresses = mutableListOf<String>()
  var newTXs = mutableListOf<Transaction>()

  private companion object {
    private val LOG = LoggerFactory.getLogger(PlasmaClient::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    super.start(startFuture)
    LOG.info("Here is my address $address")

    vertx.eventBus().send<Any>(Address.PUBLISH_ADDRESS.name, address) { response ->
      LOG.info("SUCCESS")
    }

    vertx.eventBus().consumer<Any>(Address.PUSH_ALL_ADDRESSES.name) { msg ->
      LOG.info("GOT ALL ADDRESSES")
      val allClientsAddresses = (msg.body() as JsonArray).toMutableList()

      allOtherClientsAddresses.addAll(allClientsAddresses
                              .filter { clientAddress -> clientAddress != this.address  }
                              .map { address -> address.toString() })
    }

    vertx.eventBus().consumer<Any>(Address.ISSUE_TRANSACTION.name) {
      LOG.info("ISSUE TRANSACTION MESSAGE RECEIVED")
      val tx = createTransaction()
      val txJson  = JsonObject(Json.encode(tx))
      vertx.eventBus().publish(Address.PUBLISH_TRANSACTION.name, txJson)
      println("Just published transaction")
      println(this.deploymentID())
    }

    vertx.eventBus().consumer<Any>(Address.PUBLISH_TRANSACTION.name) { msg ->
      val tx: Transaction = Json.decodeValue(msg.body().toString(), Transaction::class.java)
      println("Just recceived transaction")
      println("from: this.deploymentID()")
      if(!newTXs.contains(tx))
        newTXs.add(tx)
    }

    vertx.eventBus().consumer<Any>(Address.PUBLISH_BLOCK.name) { msg ->
      val block: PlasmaBlock = Json.decodeValue(msg.body().toString(), PlasmaBlock::class.java)
      removeUTXOsForBlock(block)
      createUTXOsForBlock(block)
      chain.addBlock(block)
      println("received block: ${HashUtils.transform(block.blockHash().toByteArray())}")
      calculateBalance()
      println("my balance: ${balance}")
      println("my utxos:")
      println(myUTXOs)
    }
  }

  override fun stop(stopFuture: Future<Void>?) {
    super.stop(stopFuture)
  }

  fun receive() {

  }

  fun send() {
    var amount = Random().nextInt(balance)
    var address = allOtherClientsAddresses.get(Random().nextInt(allOtherClientsAddresses.size))

  }

  fun createTransaction() : Transaction {
    val randomUTXO = myUTXOs.get(Random().nextInt(myUTXOs.size))
    val utxoAmount = amountFromUTXO(randomUTXO)
    val amountToSend = Random().nextInt(utxoAmount)
    var randomAddress = allOtherClientsAddresses.get(Random().nextInt(allOtherClientsAddresses.size))

    val tx = Transaction()
    tx.addInput(randomUTXO.blockNum, randomUTXO.txIndex, randomUTXO.index)
    tx.addOutput(randomAddress, amountToSend)
    tx.addOutput(address, utxoAmount - amountToSend)
    return tx
  }

  fun calculateBalancegeorgi91g() {
    for (utxo in myUTXOs) {
      val txOutput = this.chain.plasmaPool.getTxOutput(utxo)
      if(txOutput != null)
        balance += txOutput.amount
    }
  }

  fun amountFromUTXO(utxo: UTXO) : Int{
    val txOutput = this.chain.plasmaPool.getTxOutput(utxo)
    if(txOutput != null)
      return txOutput.amount
    return 0
  }

}
