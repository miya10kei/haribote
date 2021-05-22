package com.miya10kei.haribote.server

import com.miya10kei.haribote.Spec

interface Server {
  val info: ServerInfo

  fun start(specs: List<Spec>): Server
  fun stop()
}

class ServerInfo(val protocol: String, val port: Int)
