package io.plasmasimulator.plasmasimulator

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import org.slf4j.LoggerFactory

class SimulationManager : AbstractVerticle() {

  private companion object {
    private val LOGGER = LoggerFactory.getLogger(SimulationManager::class.java)
  }

  override fun start(startFuture: Future<Void>) {
    vertx
      .createHttpServer()
      .requestHandler { req ->
        req.response()
          .putHeader("content-type", "text/plain")
          .end("Hello from Plasma Simulator!")
      }
      .listen(8080) { http ->
        if (http.succeeded()) {
          startFuture.complete()
          LOGGER.info("HTTP server started on port 8080")
        } else {
          startFuture.fail(http.cause())
        }
      }

  }

}
