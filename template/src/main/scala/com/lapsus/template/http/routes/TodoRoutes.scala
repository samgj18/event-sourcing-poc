package com.lapsus.template.http.routes

import com.lapsus.template.algebra.EventStore
import com.lapsus.template.domain.command.Command.{CreateTodo, DeleteTodo, UpdateTodo}
import com.lapsus.template.domain.event.Event
import com.lapsus.template.domain.todo.Todo
import com.lapsus.template.http.util.encoder.{errorOutputEncoder, successOutputEncoder}

import cats.MonadThrow
import cats.data.Validated
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import cats.syntax.option.*
import io.circe.parser
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

case class TodoRoutes[F[_]: MonadThrow: JsonDecoder](event: EventStore[F, Todo, Event]) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/todos"

  private val httpRoutes = HttpRoutes.of[F] {
    case GET -> Root / "ping" => Ok("pong")

    case req @ POST -> Root / "create" =>
      req.asJson.attempt.flatMap {
        case Left(_) => BadRequest("Invalid json")
        case Right(json) =>
          parser.decodeAccumulating[CreateTodo](json.noSpaces) match {
            case Validated.Invalid(errors) => BadRequest(errorOutputEncoder(errors.toList.mkString(", ")))
            case Validated.Valid(command) =>
              Event.handle(command).flatMap { e =>
                event
                  .append(
                    id = e.id,
                    event = e,
                    eventType = "TodoCreated",
                  )
                  .flatMap {
                    case Left(value) => BadRequest(errorOutputEncoder(value.getMessage))
                    case Right(_)    => Ok(successOutputEncoder(e))
                  }
              }
          }
      }

    case req @ PUT -> Root / "update" =>
      req.asJson.attempt.flatMap {
        case Left(_) => BadRequest("Invalid json")
        case Right(json) =>
          parser.decodeAccumulating[UpdateTodo](json.noSpaces) match {
            case Validated.Invalid(errors) => BadRequest(errorOutputEncoder(errors.toList.mkString(", ")))
            case Validated.Valid(command) =>
              Event.handle(command).flatMap { e =>
                event
                  .append(
                    id = e.id,
                    event = e,
                    eventType = "TodoUpdated",
                  )
                  .flatMap {
                    case Left(value) => BadRequest(errorOutputEncoder(value.getMessage))
                    case Right(_)    => Ok(successOutputEncoder(e))
                  }
              }
          }
      }

    case req @ DELETE -> Root / "delete" =>
      req.asJson.attempt.flatMap {
        case Left(_) => BadRequest("Invalid json")
        case Right(json) =>
          parser.decodeAccumulating[DeleteTodo](json.noSpaces) match {
            case Validated.Invalid(errors) => BadRequest(errorOutputEncoder(errors.toList.mkString(", ")))
            case Validated.Valid(command) =>
              Event.handle(command).flatMap { e =>
                event
                  .append(
                    id = e.id,
                    event = e,
                    eventType = "TodoDeleted",
                  )
                  .flatMap {
                    case Left(value) => BadRequest(errorOutputEncoder(value.getMessage))
                    case Right(_)    => Ok(successOutputEncoder(e))
                  }
              }
          }
      }

    case GET -> Root / UUIDVar(todoId) =>
      event
        .get(
          id = s"todo-${todoId.toString}",
          when = Todo.when,
        )
        .handleError(_ => none[Todo])
        .flatMap {
          case Some(value) => Ok(successOutputEncoder(value))
          case None        => NotFound(errorOutputEncoder("Todo not found"))
        }

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
