package com.miya10kei.haribote.server

import com.github.ajalt.clikt.output.TermUi
import com.miya10kei.haribote.HttpConfiguration
import com.miya10kei.haribote.Spec
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.HttpServerExpectContinueHandler
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import java.net.URI
import java.util.concurrent.TimeUnit

class HttpServer(private val configuration: HttpConfiguration) : Server {
  private lateinit var bossGroup: NioEventLoopGroup
  private lateinit var workerGroup: NioEventLoopGroup
  private lateinit var serverChannel: Channel

  override val info: ServerInfo = ServerInfo("http", configuration.port)

  override fun start(specs: List<Spec>): Server {
    this.bossGroup = NioEventLoopGroup(1)
    this.workerGroup = NioEventLoopGroup()
    val logEnabled = configuration.logEnabled
    try {
      this.serverChannel = ServerBootstrap()
        .option(ChannelOption.SO_BACKLOG, 1024)
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel::class.java).run {
          if (logEnabled) handler(LoggingHandler(LogLevel.INFO)) else this
        }
        .childHandler(object : ChannelInitializer<SocketChannel>() {
          override fun initChannel(ch: SocketChannel) {
            ch.pipeline()
              .addLast(HttpServerCodec())
              .addLast(HttpServerExpectContinueHandler())
              .addLast(HttpMockHandler(specs))
          }
        })
        .bind(configuration.port)
        .sync()
        .channel()
    } catch (e: Exception) {
      this.stop()
      throw e
    }
    return this
  }

  override fun stop() {
    try {
      this.serverChannel.close().sync()
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }
}

private class HttpMockHandler(private val specs: List<Spec>) : SimpleChannelInboundHandler<HttpObject>() {

  private val notFoundResponse: HttpResponse = DefaultFullHttpResponse(
    HttpVersion.HTTP_1_1,
    HttpResponseStatus.NOT_FOUND,
  ).apply {
    this.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0)
  }

  private val internalServerErrorResponse: HttpResponse = DefaultFullHttpResponse(
    HttpVersion.HTTP_1_1,
    HttpResponseStatus.INTERNAL_SERVER_ERROR
  ).apply {
    this.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0)
  }

  override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
    if (msg is HttpRequest) {
      val path = URI.create(msg.uri()).path
      val method = msg.method().name()
      val spec = specs.find {
        path.matches(Regex(it.path)) && method.equals(it.method.name, true)
      }

      val response = spec?.toHttpResponse() ?: notFoundResponse

      val isKeepAlive = HttpUtil.isKeepAlive(msg)
      if (isKeepAlive) {
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
      } else {
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
      }

      ctx.executor().schedule(
        {
          ctx.writeAndFlush(response).apply {
            if (!isKeepAlive) this.addListener(ChannelFutureListener.CLOSE)
          }
        },
        spec?.delayMills ?: 0L,
        TimeUnit.MILLISECONDS
      )
    }
  }

  override fun channelReadComplete(ctx: ChannelHandlerContext) {
    ctx.flush()
  }

  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    TermUi.echo(message = cause.message, err = true)
    ctx.writeAndFlush(internalServerErrorResponse)
  }

  private fun Spec.byteBody(): ByteBuf =
    Unpooled.wrappedBuffer(this.body.toByteArray())

  private fun Spec.toHttpResponse(): HttpResponse =
    DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.parseLine(this.status.toString()),
      this.byteBody()
    ).also {
      it.headers()
        .setInt(HttpHeaderNames.CONTENT_LENGTH, it.content().readableBytes())
        .set(HttpHeaderNames.CONTENT_TYPE, this.contentType)
    }
}
