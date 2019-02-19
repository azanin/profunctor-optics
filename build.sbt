import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")

scalacOptions += "-Ypartial-unification"

lazy val root = (project in file("."))
  .settings(
    name := "profunctor-optics",
    libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "1.6.0",
    "org.scalatest" %% "scalatest" % "3.0.5" % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
