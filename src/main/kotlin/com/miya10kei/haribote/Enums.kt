package com.miya10kei.haribote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Protocol {
  @SerialName("http")
  HTTP
}

@Serializable
enum class HttpMethod {
  @SerialName("get")
  GET,

  @SerialName("post")
  POST,

  @SerialName("patch")
  PATCH,

  @SerialName("delete")
  DELETE,
}
