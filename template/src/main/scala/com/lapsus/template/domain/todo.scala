package com.lapsus.template.domain

import com.lapsus.template.domain.event.Event

import derevo.cats.{eqv, show}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.auto.*
import eu.timepit.refined.cats.*
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.semiauto.*
import io.circe.refined.*
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

object todo {

  @derive(show, eqv, decoder, encoder)
  @newtype case class TodoId(value: String)

  @derive(show, eqv, decoder, encoder)
  @newtype case class TodoTitle(value: String)

  @derive(show, eqv, decoder, encoder)
  @newtype case class TodoDescription(value: String)

  @derive(show, eqv, decoder, encoder)
  @newtype case class TodoStatus(value: String)

  @derive(show, eqv, decoder, encoder)
  @newtype case class TodoPriority(value: String)

  @derive(show, eqv)
  case class Todo(
    id: TodoId,
    title: TodoTitle,
    description: TodoDescription,
    status: TodoStatus,
    priority: TodoPriority,
  )

  object Todo {

    implicit val todoEncoder: Encoder[Todo] = deriveEncoder[Todo]
    implicit val todoDecoder: Decoder[Todo] = deriveDecoder[Todo]

    val empty: Todo = Todo(
      id = TodoId(""),
      title = TodoTitle(""),
      description = TodoDescription(""),
      status = TodoStatus(""),
      priority = TodoPriority(""),
    )

    def when(entity: Todo, event: Event): Todo =
      event match {
        case Event.TodoCreated(id, title, description, status, priority) =>
          entity
            .copy(
              id = id,
              title = title,
              description = description,
              status = status,
              priority = priority,
            )
        case Event.TodoUpdated(id, title, description, status, priority) =>
          entity.copy(
            id = id,
            title = title,
            description = description,
            status = status,
            priority = priority,
          )
        case Event.TodoDeleted(id) =>
          entity.copy(
            id = id
          )
      }

  }

  @derive(show, eqv, decoder, encoder)
  @newtype case class TodoIdParam(value: NonEmptyString) {
    def toDomain: TodoId = TodoId(value.value)
  }

  @derive(show, eqv, decoder, encoder)
  @newtype case class TodoTitleParam(value: NonEmptyString) {
    def toDomain: TodoTitle = TodoTitle(value.value)
  }

  @derive(decoder, encoder, show, eqv)
  @newtype case class TodoDescriptionParam(value: NonEmptyString) {
    def toDomain: TodoDescription = TodoDescription(value.value)
  }

  @derive(show, eqv, decoder, encoder)
  @newtype case class TodoStatusParam(value: NonEmptyString) {
    def toDomain: TodoStatus = TodoStatus(value.value)
  }

  @derive(show, eqv, decoder, encoder)
  @newtype case class TodoPriorityParam(value: NonEmptyString) {
    def toDomain: TodoPriority = TodoPriority(value.value)
  }

}
