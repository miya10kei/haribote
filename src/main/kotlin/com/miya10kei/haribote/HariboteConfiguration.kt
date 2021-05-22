package com.miya10kei.haribote

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
class HariboteConfiguration(
  val server: ServerConfiguration
) {
  companion object {
    fun load(file: File): HariboteConfiguration =
      Yaml(configuration = YamlConfiguration(strictMode = false))
        .decodeFromString(this.serializer(), file.readText())
  }
}

@Serializable
class ServerConfiguration(
  val http: HttpConfiguration
)

@Serializable
class HttpConfiguration(
  val port: Int,
  @SerialName("log-enabled")
  val logEnabled: Boolean
)
