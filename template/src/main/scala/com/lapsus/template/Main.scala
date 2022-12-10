import com.lapsus.template.algebra.EventStore
import com.lapsus.template.http.routes.TodoRoutes
import com.lapsus.template.http.vars.version

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.IpLiteralSyntax
import dolphin.StoreSession
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  val program: Stream[IO, Server] =
    for {
      session <- StoreSession.stream[IO]("localhost", 2113, tls = false)
      // keeping the session open starves the cpu, we might want to open it only when needed and set a timeout for it
      eventStore = EventStore.make[IO](session)
      routes = Router(
        version -> TodoRoutes[IO](eventStore).routes
      )
      http4s <- Stream.eval(
        EmberServerBuilder
          .default[IO]
          .withHost(host"localhost")
          .withPort(port"8080")
          .withHttpApp(routes.orNotFound)
          .build
          .useForever
      )

    } yield http4s

  // start program and run forever
  def run: IO[Unit] = program.compile.drain

}
