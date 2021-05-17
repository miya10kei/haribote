package com.miya10kei.haribote

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

fun main(args: Array<String>) = Haribote().main(args)

class Haribote : CliktCommand() {

  private val specFile: File by option("-f", "--file")
    .file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true, canBeSymlink = true)
    .default(Configuration.defaultSpecFile)

  override fun run() {
    val specs = Specs.load(this.specFile)

    val hariboteConfiguration = HariboteConfiguration.load(Configuration.hariboteFile)
    val server = Server(hariboteConfiguration.server).start(specs)

    TermUi.repeatUntilEnteringYes("Do you want to stop?") {
      server.stop()
    }
  }
}
