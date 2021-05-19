package com.miya10kei.haribote.server

import com.miya10kei.haribote.ServerConfiguration
import com.miya10kei.haribote.Specs

class Servers(private val configuration: ServerConfiguration) {
  private val servers: MutableList<Server> = mutableListOf()

  fun start(specs: Specs): Servers {
    if (specs.hasHttpSpec()) {
      servers.add(
        HttpServer(configuration.http).start(specs.httpSpecs)
      )
    }
    return this
  }

  fun stop() {
    servers.forEach { it.stop() }
  }
}
