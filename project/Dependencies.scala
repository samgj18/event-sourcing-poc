import sbt._

object Dependencies {

  object V {
    val cats       = "2.9.0"
    val catsEffect = "3.4.1"
    val circe      = "0.14.3"
    val eventStore = "0.0-16919d4-SNAPSHOT"
    val derevo     = "0.13.0"
    val http4s     = "0.23.16"
    val fs2        = "3.4.0"
    val log4cats   = "2.5.0"
    val newtype    = "0.4.4"
    val refined    = "0.10.1"

    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.13.2"
    val logback          = "1.4.5"
    val organizeImports  = "0.6.0"
    val semanticDB       = "4.6.0"

    val weaver = "0.8.1"
  }

  object Libraries {
    def circe(artifact: String): ModuleID  = "io.circe"   %% s"circe-$artifact"  % V.circe
    def derevo(artifact: String): ModuleID = "tf.tofu"    %% s"derevo-$artifact" % V.derevo
    def http4s(artifact: String): ModuleID = "org.http4s" %% s"http4s-$artifact" % V.http4s

    val cats: ModuleID            = "org.typelevel"      %% "cats-core"     % V.cats
    val catsEffect: ModuleID      = "org.typelevel"      %% "cats-effect"   % V.catsEffect
    val eventStore: ModuleID      = "io.github.lapsushq" %% "dolphin-core"  % V.eventStore
    val eventStoreCirce: ModuleID = "io.github.lapsushq" %% "dolphin-circe" % V.eventStore
    val fs2: ModuleID             = "co.fs2"             %% "fs2-core"      % V.fs2

    val circeCore: ModuleID    = circe("core")
    val circeGeneric: ModuleID = circe("generic")
    val circeParser: ModuleID  = circe("parser")
    val circeRefined: ModuleID = circe("refined")

    val derevoCore: ModuleID  = derevo("core")
    val derevoCats: ModuleID  = derevo("cats")
    val derevoCirce: ModuleID = derevo("circe-magnolia")

    val http4sDsl: ModuleID    = http4s("dsl")
    val http4sServer: ModuleID = http4s("ember-server")
    val http4sCirce: ModuleID  = http4s("circe")

    val refinedCore: ModuleID = "eu.timepit" %% "refined"      % V.refined
    val refinedCats: ModuleID = "eu.timepit" %% "refined-cats" % V.refined

    val log4cats: ModuleID = "org.typelevel" %% "log4cats-slf4j" % V.log4cats
    val newtype: ModuleID  = "io.estatico"   %% "newtype"        % V.newtype

    // Runtime
    val logback: ModuleID = "ch.qos.logback" % "logback-classic" % V.logback

    // Test
    val log4catsNoOp: ModuleID      = "org.typelevel"       %% "log4cats-noop"      % V.log4cats
    val refinedScalacheck: ModuleID = "eu.timepit"          %% "refined-scalacheck" % V.refined
    val weaverCats: ModuleID        = "com.disneystreaming" %% "weaver-cats"        % V.weaver
    val weaverDiscipline: ModuleID  = "com.disneystreaming" %% "weaver-discipline"  % V.weaver
    val weaverScalaCheck: ModuleID  = "com.disneystreaming" %% "weaver-scalacheck"  % V.weaver

    // Scalafix rules
    val organizeImports = "com.github.liancheng" %% "organize-imports" % V.organizeImports
  }

  object CompilerPlugin {

    val betterMonadicFor = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % V.betterMonadicFor
    )

    val kindProjector = compilerPlugin(
      "org.typelevel" % "kind-projector" % V.kindProjector cross CrossVersion.full
    )

    val semanticDB = compilerPlugin(
      "org.scalameta" % "semanticdb-scalac" % V.semanticDB cross CrossVersion.full
    )

  }

}
