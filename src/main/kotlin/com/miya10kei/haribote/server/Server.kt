package com.miya10kei.haribote.server

import com.miya10kei.haribote.Spec

interface Server {
  fun start(specs: List<Spec>): Server
  fun stop()
}

