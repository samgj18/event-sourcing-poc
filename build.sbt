import Dependencies._

ThisBuild / scalaVersion                        := "2.13.10"
ThisBuild / version                             := "0.0.1"
ThisBuild / organization                        := "com.lapsus"
ThisBuild / organizationName                    := "Lapsus"
ThisBuild / evictionErrorLevel                  := Level.Warn
ThisBuild / githubWorkflowJavaVersions          := Seq(JavaSpec.temurin("17"))
ThisBuild / versionScheme                       := Some("early-semver")
ThisBuild / githubWorkflowPublishTargetBranches := Seq()
ThisBuild / scalafixDependencies += Libraries.organizeImports

resolvers ++= Resolver.sonatypeOssRepos("snapshots")

val pName = "template"

val scalafixCommonSettings = inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest))

lazy val root = (project in file(pName))
  .configs(IntegrationTest)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    name                 := pName,
    Docker / packageName := pName,
    scalafmtOnCompile    := true,
    scalacOptions ++= List("-Xsource:3", "-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info"),
    resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
    Defaults.itSettings,
    scalafixCommonSettings,
    dockerBaseImage := "openjdk:17-jre-slim-buster",
    dockerExposedPorts ++= Seq(8080),
    makeBatScripts     := Seq(),
    dockerUpdateLatest := true,
    libraryDependencies ++= Seq(
      CompilerPlugin.kindProjector,
      CompilerPlugin.betterMonadicFor,
      CompilerPlugin.semanticDB,
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.catsRetry,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeParser,
      Libraries.circeRefined,
      Libraries.cirisCore,
      Libraries.cirisEnum,
      Libraries.cirisRefined,
      Libraries.derevoCore,
      Libraries.derevoCats,
      Libraries.derevoCirce,
      Libraries.eventStore,
      Libraries.eventStoreCirce,
      Libraries.fs2,
      Libraries.http4sDsl,
      Libraries.http4sServer,
      Libraries.http4sClient,
      Libraries.http4sCirce,
      Libraries.http4sJwtAuth,
      Libraries.javaxCrypto,
      Libraries.log4cats,
      Libraries.logback % Runtime,
      Libraries.monocleCore,
      Libraries.newtype,
      Libraries.redis4catsEffects,
      Libraries.redis4catsLog4cats,
      Libraries.refinedCore,
      Libraries.refinedCats,
      Libraries.skunkCore,
      Libraries.skunkCirce,
      Libraries.squants,

      // Testing
      CompilerPlugin.kindProjector,
      CompilerPlugin.betterMonadicFor,
      CompilerPlugin.semanticDB,
      Libraries.catsLaws,
      Libraries.log4catsNoOp,
      Libraries.monocleLaw,
      Libraries.refinedScalacheck,
      Libraries.weaverCats,
      Libraries.weaverDiscipline,
      Libraries.weaverScalaCheck,
    ),
  )

addCommandAlias("lint", "scalafmtAll;scalafixAll --rules OrganizeImports; scalafmtSbt")
addCommandAlias("build", "clean; all scalafmtCheck scalafmtSbtCheck compile test githubWorkflowCheck")
