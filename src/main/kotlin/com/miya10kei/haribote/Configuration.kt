package com.miya10kei.haribote

import java.io.File

object Configuration {
  private const val hariboteFilename = "haribote.yaml"
  private const val defaultSpecFilename = "default-spec.yaml"

  private val baseDir: File = File("${System.getenv("HOME")}/.config/haribote")

  val hariboteFile: File = baseDir.resolve(hariboteFilename)
  val defaultSpecFile: File = baseDir.resolve(defaultSpecFilename)

  init {
    if (!baseDir.exists()) {
      baseDir.mkdirs()
    }

    if (!defaultSpecFile.exists()) {
      this.javaClass.classLoader.getResource("./$defaultSpecFilename")
        ?.readText()
        ?.let { defaultSpecFile.writeText(it) }
    }

    if (!hariboteFile.exists()) {
      this.javaClass.classLoader.getResource("./$hariboteFilename")
        ?.readText()
        ?.let { hariboteFile.writeText(it) }
    }
  }
}
