package com.lapsus.template.domain

import java.util.UUID

import com.lapsus.template.domain.command.Command
import com.lapsus.template.domain.todo.*

import cats.Applicative
import cats.syntax.applicative.*
import cats.syntax.functor.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}

object event {

  sealed trait Event extends Product with Serializable {
    def id: String
  }

  object Event {

    def UUIDGenerator[F[_]: Applicative, A](
      fa: String => A
    ): F[A] = UUID.randomUUID().pure[F].map { uuid =>
      fa(uuid.toString)
    }

    implicit val eventDecoder: Decoder[Event] =
      TodoCreated.todoCreatedDecoder.widen[Event] or
        TodoUpdated.todoUpdatedDecoder.widen[Event] or
        TodoDeleted.todoDeletedDecoder.widen[Event]

    implicit val eventEncoder: Encoder[Event] = Encoder.instance {
      case todoCreated: TodoCreated => todoCreated.asJson
      case todoUpdated: TodoUpdated => todoUpdated.asJson
      case todoDeleted: TodoDeleted => todoDeleted.asJson
    }

    final case class TodoCreated(
      todoId: TodoId,
      title: TodoTitle,
      description: TodoDescription,
      status: TodoStatus,
      priority: TodoPriority,
    ) extends Event {
      override def id: String = s"todo-${todoId.value}"
    }

    object TodoCreated {
      implicit val todoCreatedEncoder: Encoder[TodoCreated] = deriveEncoder[TodoCreated]
      implicit val todoCreatedDecoder: Decoder[TodoCreated] = deriveDecoder[TodoCreated]
    }

    final case class TodoUpdated(
      todoId: TodoId,
      title: TodoTitle,
      description: TodoDescription,
      status: TodoStatus,
      priority: TodoPriority,
    ) extends Event {
      override def id: String = s"todo-${todoId.value}"
    }

    object TodoUpdated {
      implicit val todoUpdatedEncoder: Encoder[TodoUpdated] = deriveEncoder[TodoUpdated]
      implicit val todoUpdatedDecoder: Decoder[TodoUpdated] = deriveDecoder[TodoUpdated]
    }

    final case class TodoDeleted(
      todoId: TodoId
    ) extends Event {
      override def id: String = s"todo-${todoId.value}"
    }

    object TodoDeleted {
      implicit val todoDeletedEncoder: Encoder[TodoDeleted] = deriveEncoder[TodoDeleted]
      implicit val todoDeletedDecoder: Decoder[TodoDeleted] = deriveDecoder[TodoDeleted]
    }

    def handle[F[_]: Applicative](command: Command): F[Event] =
      command match {
        case Command.UpdateTodo(id, title, description, status, priority) =>
          Applicative[F].pure(
            Event.TodoUpdated(id.toDomain, title.toDomain, description.toDomain, status.toDomain, priority.toDomain)
          )

        case Command.CreateTodo(title, description, status, priority) =>
          UUIDGenerator(TodoId.apply).map { uuid =>
            Event.TodoCreated(uuid, title.toDomain, description.toDomain, status.toDomain, priority.toDomain)
          }

        case Command.DeleteTodo(id) =>
          Applicative[F].pure(
            Event
              .TodoDeleted(id.toDomain)
          )

      }

  }

}
