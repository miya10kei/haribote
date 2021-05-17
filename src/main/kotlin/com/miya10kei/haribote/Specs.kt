package com.miya10kei.haribote

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
class Specs(
  @SerialName("specs")
  val values: List<Spec> = listOf(Spec())
) {
  companion object {
    fun load(file: File): Specs =
      Yaml(configuration = YamlConfiguration(strictMode = false))
        .decodeFromString(this.serializer(), file.readText())
  }

  val httpSpecs: List<Spec>
    get() = this.values.filter { it.protocol == Protocol.HTTP }

  fun hasHttpSpec(): Boolean =
    this.values.any { it.protocol == Protocol.HTTP }
}

@Serializable
class Spec(
  val protocol: Protocol = Protocol.HTTP,
  val method: HttpMethod = HttpMethod.GET,
  val path: String = "/",
  val status: Int = 200,
  @SerialName("content-type")
  val contentType: String = "application/json",
  val body: String = "Hello World",
  @SerialName("delay-mills")
  val delayMills: Long = 0L
)
