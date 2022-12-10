package com.lapsus.template.domain

import com.lapsus.template.domain.todo.*

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

object command {
  sealed trait Command extends Product with Serializable

  object Command {

    final case class CreateTodo(
      title: TodoTitleParam,
      description: TodoDescriptionParam,
      status: TodoStatusParam,
      priority: TodoPriorityParam,
    ) extends Command

    object CreateTodo {
      implicit val createTodoEncoder: Encoder[CreateTodo] = deriveEncoder[CreateTodo]
      implicit val createTodoDecoder: Decoder[CreateTodo] = deriveDecoder[CreateTodo]
    }

    final case class UpdateTodo(
      id: TodoIdParam,
      title: TodoTitleParam,
      description: TodoDescriptionParam,
      status: TodoStatusParam,
      priority: TodoPriorityParam,
    ) extends Command

    object UpdateTodo {
      implicit val updateTodoEncoder: Encoder[UpdateTodo] = deriveEncoder[UpdateTodo]
      implicit val updateTodoDecoder: Decoder[UpdateTodo] = deriveDecoder[UpdateTodo]
    }

    final case class DeleteTodo(
      id: TodoIdParam
    ) extends Command

    object DeleteTodo {
      implicit val deleteTodoEncoder: Encoder[DeleteTodo] = deriveEncoder[DeleteTodo]
      implicit val deleteTodoDecoder: Decoder[DeleteTodo] = deriveDecoder[DeleteTodo]
    }

  }

}
