package io.plasmasimulator.utils

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.OpenOptions

class FileManager {
  companion object {
    fun writeFile(vertx: Vertx, input: String, file: String) {
      vertx.fileSystem().open("json/"+file, OpenOptions().setAppend(true)) { ar ->
        if (ar.succeeded()) {
          val ws = ar.result()
          val inputBuffer = Buffer.buffer("\n"+input)
          ws.write(inputBuffer)
        } else {
          println("Could not open file")
        }
      }
    }

    fun writeNewFile(vertx: Vertx, input: String, file: String) {
      vertx.fileSystem().open("json/"+file, OpenOptions().setAppend(false)) { ar ->
        if (ar.succeeded()) {
          val ws = ar.result()
          val inputBuffer = Buffer.buffer("\n"+input)
          ws.write(inputBuffer)
        } else {
          println("Could not open file")
        }
      }
    }
  }
}
