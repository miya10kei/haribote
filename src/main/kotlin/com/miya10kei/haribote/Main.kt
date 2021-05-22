package com.miya10kei.haribote

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.miya10kei.haribote.server.Servers
import java.io.File

fun main(args: Array<String>) = Haribote().main(args)

class Haribote : CliktCommand() {

  private val specFile: File by option("-f", "--file")
    .file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true, canBeSymlink = true)
    .default(Configuration.defaultSpecFile)

  override fun run() {
    val specs = Specs.load(this.specFile)

    val hariboteConfiguration = HariboteConfiguration.load(Configuration.hariboteFile)
    val servers = Servers(hariboteConfiguration.server).start(specs)

    servers.info().forEach {
      TermUi.echo("${it.protocol} server is listening on ${it.port} 🎧")
    }

    TermUi.repeatUntilEnteringYes("Do you wanna stop ❓") {
      servers.stop()
    }
  }
}
