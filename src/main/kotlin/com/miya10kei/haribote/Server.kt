package com.miya10kei.haribote

import io.netty.handler.codec.http.HttpHeaderNames
import reactor.core.publisher.Mono
import reactor.netty.DisposableServer
import reactor.netty.http.server.HttpServer
import reactor.netty.http.server.HttpServerRequest
import reactor.netty.http.server.HttpServerResponse
import java.time.Duration

class Server(
  private val configuration: ServerConfiguration
) {
  private val servers = mutableListOf<DisposableServer>()

  fun start(specs: Specs): Server {
    if (specs.hasHttpSpec()) {
      servers.add(
        startHttpServer(specs.httpSpecs, this.configuration.http)
      )
    }
    return this
  }

  fun stop() =
    servers.forEach {
      it.disposeNow(Duration.ofMillis(3_000L))
    }

  private fun startHttpServer(httpSpecs: List<Spec>, configuration: HttpConfiguration): DisposableServer =
    HttpServer.create()
      .port(configuration.port)
      .accessLog(configuration.enabledLog)
      .compress(configuration.compress)
      .route { builder ->
        httpSpecs.forEach { spec ->
          val handler = { _: HttpServerRequest, res: HttpServerResponse ->
            res.status(spec.status)
              .addHeader(HttpHeaderNames.CONTENT_TYPE, spec.contentType)
              .sendString(
                Mono.just(spec.body)
                  .delayElement(Duration.ofMillis(spec.delayMills))
              )
          }
          when (spec.method) {
            HttpMethod.GET -> builder.get(spec.path, handler)
            HttpMethod.POST -> builder.post(spec.path, handler)
            HttpMethod.PATCH -> builder.post(spec.path, handler)
            HttpMethod.DELETE -> builder.post(spec.path, handler)
          }
        }
      }
      .bindNow()
}
