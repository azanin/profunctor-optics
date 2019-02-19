import Dependencies._

addCommandAlias("cc", "all clean compile")
addCommandAlias("c", "compile")
addCommandAlias("ps", "projects")
addCommandAlias("p", "project")

lazy val scalacSettings = Seq(
  "-encoding",
  "UTF-8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-explaintypes",
  "-opt-warnings",
  "-language:existentials",
  "-language:higherKinds",
  "-opt:l:inline",
  "-opt-inline-from:<source>",
  "-Ypartial-unification",
  "-Yrangepos",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-extra-implicit",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-unused:_,-imports",
  "-Xsource:2.13",
  "-Xlint:_,-type-parameter-shadow",
  "-Xfuture",
  "-Xfatal-warnings"
)

val resolver = Resolver.sonatypeRepo("releases")

val settings = Seq(
  scalaVersion := "2.12.8",
  version := "0.1.0-SNAPSHOT",
  organization := "com.example",
  organizationName := "example",
  scalacOptions ++= scalacSettings,
  libraryDependencies ++= Seq("org.typelevel" %% "cats-core" % "1.6.0"),
  resolvers += resolver,
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")
)

lazy val root = (project in file("."))
  .settings(
    name := "profunctor-optics",
    settings
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
