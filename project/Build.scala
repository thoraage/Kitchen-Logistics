import com.earldouglas.xwp.XwpPlugin
import sbt._
import Keys._

object Build extends Build {
  import Dependencies._

  lazy val myProject = Project("kitchen-logistics", file("."))
    .settings(
      organization  := "com.example",
      version       := "0.9.0",
      scalaVersion  := "2.10.2",
      scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
      resolvers    ++= Seq("Maven repo" at "http://central.maven.org/maven2/"),
      libraryDependencies ++= Seq(
        "net.databinder"            %% "unfiltered-directives" % V.unfiltered,
        "net.databinder"            %% "unfiltered-jetty" % V.unfiltered,
        "net.databinder"            %% "unfiltered-filter" % V.unfiltered,
        "net.databinder"            %% "unfiltered-directives" % V.unfiltered,
        "net.databinder"            %% "unfiltered-json4s" % V.unfiltered,
        "net.databinder"            %% "unfiltered-spec" % V.unfiltered % "test",
        "com.typesafe.slick"        %% "slick" % "1.0.1",
        "com.github.tototoshi"      %% "slick-joda-mapper" % "0.4.0",
        "javax.servlet"             % "servlet-api"      % "2.3" % "provided",
        "com.h2database"            %  "h2"              % "1.3.166" % "compile",
        "org.postgresql"            % "postgresql"       % "9.4-1200-jdbc41" % "compile",
        "c3p0"                      %  "c3p0"            % "0.9.1.2" % "compile",
        "org.flywaydb" % "flyway-core" % "3.1" % "compile",
        "org.specs2"                %% "specs2"          % V.specs2  % "test",
        "org.slf4j"                 %  "slf4j-api"       % V.slf4j,
        "ch.qos.logback"            %  "logback-classic" % V.logback
      )
    )
    .settings(XwpPlugin.jetty() :_*)
}

object Dependencies {

  object V {
    val specs2  = "2.2"
    val jetty   = "8.1.0.v20120127"
    val slf4j   = "1.6.4"
    val logback = "1.0.0"
    val unfiltered = "0.7.0"
  }
}