package com.lapsus.template.algebra

import com.lapsus.template.domain.event.Event
import com.lapsus.template.domain.event.Event.{TodoCreated, TodoDeleted, TodoUpdated}
import com.lapsus.template.domain.todo.Todo

import cats.effect.Async
import cats.syntax.applicativeError.*
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.option.*
import dolphin.StoreSession
import dolphin.circe.domain.ReadDecodeResult
import dolphin.circe.domain.ReadDecodeResult.{Failure, Success}
import dolphin.circe.syntax.reader.*
import dolphin.option.{ReadOptions, WriteOptions}
import fs2.Stream
import io.circe.syntax.*
import org.typelevel.log4cats.Logger

trait EventStore[F[_], TEntity, TEvent] {
  def get(id: String, when: (TEntity, TEvent) => TEntity): F[Option[TEntity]]

  def append(id: String, event: TEvent, eventType: String): F[Either[Throwable, Unit]]

  def append(id: String, event: TEvent, eventType: String, version: Long): F[Either[Throwable, Unit]]
}

object EventStore {

  def make[F[_]: Async: Logger](session: StoreSession[F]): EventStore[F, Todo, Event] =
    new EventStore[F, Todo, Event] {

      override def get(
        id: String,
        when: (Todo, Event) => Todo,
      ): F[Option[Todo]] = session.read(id, ReadOptions.default.fromStart.withMaxCount(100).forward).flatMap { result =>
        def mapResult(result: ReadDecodeResult[Event]): Stream[F, Option[Todo]] =
          result match {
            case Success(value)  => Stream(Some(when(Todo.empty, value)))
            case Failure(errors) => Stream.eval(Logger[F].error(s"$errors")) *> Stream(none[Todo])
          }

        result
          .getEventType
          .flatMap {
            case "TodoCreated" => result.as[TodoCreated].flatMap(mapResult)
            case "TodoUpdated" => result.as[TodoUpdated].flatMap(mapResult)
            case "TodoDeleted" => result.as[TodoDeleted].flatMap(mapResult)
            case _             => Stream(none[Todo])
          }
          .compile
          .lastOrError

      }

      override def append(
        id: String,
        event: Event,
        eventType: String,
      ): F[Either[Throwable, Unit]] = session.write(id, event.asJson.noSpaces.getBytes, eventType).attempt.as(Right(()))

      override def append(id: String, event: Event, eventType: String, version: Long): F[Either[Throwable, Unit]] =
        session
          .write(id, WriteOptions.default.withExpectedRevision(version), event.asJson.noSpaces.getBytes, eventType)
          .attempt
          .as(Right(()))

    }

}
