package controllers

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class WebSocketControllerSpec extends PlaySpec with GuiceOneServerPerSuite {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val incoming: Sink[Message, Future[Done]] = Sink.foreach {
    case message: TextMessage =>
      Await.result(message.textStream.runFold("")(_ + _), Duration.Inf) mustBe "Hello!"
  }

  private val sourceCloseAfterMessage: Source[Message, NotUsed] = Source.single(TextMessage("Hi!"))
  private val sourceKeepOpens = sourceCloseAfterMessage.concatMat(Source.maybe[Message])(Keep.right)

  "WebSocket server" should {
    "not close the connection without close parameter - client should close connection" in {
      val (_, closed) = sourceCloseAfterMessage
        .viaMat(buildWebSocketFlow(None))(Keep.right)
        .toMat(incoming)(Keep.both).run()

      Await.result(closed, 1.seconds)
    }

    "not close the connection with close parameter set to false - client should close connection" in {
      val (_, closed) = sourceCloseAfterMessage
        .viaMat(buildWebSocketFlow(Some(false)))(Keep.right)
        .toMat(incoming)(Keep.both).run()

      Await.result(closed, 1.seconds)
    }

    "close the connection with close parameter set to true - client should not close connection" in {
      val (_, closed) = sourceKeepOpens
        .viaMat(buildWebSocketFlow(Some(true)))(Keep.right)
        .toMat(incoming)(Keep.both).run()

      Await.result(closed, 1.seconds)
    }
  }

  private def buildWebSocketFlow(close: Option[Boolean]) = {
    val parameters = close.fold("")(b => s"?close=$b")
    Http().webSocketClientFlow(WebSocketRequest(s"ws://localhost:$port$parameters"))
  }
}
