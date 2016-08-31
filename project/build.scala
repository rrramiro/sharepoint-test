import sbtscalaxb.Plugin.ScalaxbKeys.{scalaxb, async, packageName, packageNames, dispatchVersion}
import sbtscalaxb.Plugin.scalaxbSettings

import sbt._
import sbt.Keys._

object build extends sbt.Build {

  lazy val dispatchV = "0.11.3"

  lazy val root = (project in file("."))
    .settings(scalaxbSettings: _*)
    .settings(
      organization  := "com.example",
      scalaVersion  := "2.11.8",
      name := "SharePointTest",
      libraryDependencies ++= Seq(
        "org.slf4j" % "jcl-over-slf4j" % "1.7.14",
        "org.slf4j" % "slf4j-simple" % "1.7.14",
        "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1",
        "org.scalatest" %% "scalatest" % "2.2.6" % "test",
        "net.databinder.dispatch" %% "dispatch-core" % dispatchV
      ),
      sourceGenerators in Compile <+= scalaxb in Compile,
      dispatchVersion in scalaxb in Compile := dispatchV,
      async in (Compile, scalaxb)           := true,
      packageName in scalaxb in Compile     := "com.microsoft.sharepoint.ws",
      //packageNames in (Compile, scalaxb)    := Map(uri("http://schemas.microsoft.com/sharepoint/soap/SlideLibrary/") -> "com.microsoft.sharepoint.ws.office"),
      logLevel in (Compile, scalaxb) := Level.Warn
    )
}
