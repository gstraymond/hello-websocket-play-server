package controllers

import akka.stream.scaladsl.{Flow, Sink, Source}
import javax.inject.Inject
import play.api.Logger
import play.api.mvc.{InjectedController, WebSocket}

class WebSocketController @Inject()() extends InjectedController {

  private val log = Logger(getClass)

  private val helloSource = Source.single("Hello!")
  private val logSink = Sink.foreach { s: String => log.info(s"received: $s") }

  def hello(close: Option[Boolean]): WebSocket = WebSocket.accept[String, String] { _ =>
    val closeAfterMessage = close.getOrElse(false)
    Flow.fromSinkAndSource(
      logSink,
      // server close connection after sending message
      if (closeAfterMessage) helloSource
      // keep connection open
      else helloSource.concat(Source.maybe)
    )
  }
}
