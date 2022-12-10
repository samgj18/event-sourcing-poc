package com.lapsus.template.http.util

import io.circe.syntax.*
import io.circe.{Encoder, Json}

object encoder {

  def errorOutputEncoder(error: String): Json = Json.obj(
    "type"  -> Json.fromString("error"),
    "error" -> Json.fromString(error),
  )

  def successOutputEncoder[A: Encoder](value: A): Json = Json.obj(
    "type"  -> Json.fromString("success"),
    "value" -> value.asJson,
  )

}
