import sbt._
import com.github.siasia._
import PluginKeys._
import Keys._

object Build extends sbt.Build {

  lazy val myProject = Project("spray-template", file("."))
    .settings(WebPlugin.webSettings: _*)
    .settings(port in config("container") := 8080)
    .settings(
    organization := "com.example",
    version := "0.9.0",
    scalaVersion := "2.9.1",
    scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
    resolvers ++= Dependencies.resolutionRepos,
    libraryDependencies ++= Seq(
      Seq(
        "net.databinder" %% "unfiltered" % "0.6.4",
        "net.databinder" %% "unfiltered-filter" % "0.6.4",
        "net.databinder" %% "unfiltered-json" % "0.6.4",
        "net.databinder" %% "unfiltered-agents" % "0.6.4",
        "org.scalaquery" %% "scalaquery" % "0.10.0-M1",
        "com.h2database" % "h2" % "1.3.166",
        "net.databinder" %% "unfiltered-jetty" % "0.6.4",
        "c3p0" % "c3p0" % "0.9.1.2"
      ).map(_ % "compile"),
      Seq("javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided->default"),
      Seq(
        //"org.specs2" %% "specs2" % "1.7.1",
        "net.databinder" %% "unfiltered-spec" % "0.6.4"
      ).map(_ % "test"),
      Seq(
        "org.eclipse.jetty" % "jetty-webapp" % "8.1.0.v20120127",
        "org.slf4j" % "slf4j-api" % "1.6.4",
        "ch.qos.logback" % "logback-classic" % "1.0.0"
      ).map(_ % "container")
    ).flatten
  )
}

object Dependencies {
  val resolutionRepos = Seq(
    ScalaToolsSnapshots,
    "Typesafe repo" at "http://repo.typesafe.com/typesafe/releases/"
  )
}