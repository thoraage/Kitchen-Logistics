import NativePackagerKeys._

packageArchetype.java_application

name := "kitlog"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
    "net.databinder"            %% "unfiltered-directives" % "0.8.4",
    "net.databinder"            %% "unfiltered-jetty" % "0.8.4",
    "net.databinder"            %% "unfiltered-filter" % "0.8.4",
    "net.databinder"            %% "unfiltered-directives" % "0.8.4",
    "net.databinder"            %% "unfiltered-json4s" % "0.8.4",
    "org.json4s"                %% "json4s-jackson" % "3.2.9",
    "com.typesafe.slick"        %% "slick" % "2.1.0",
    "com.h2database"            %  "h2"              % "1.4.191" % "compile",
    "org.postgresql"            % "postgresql"       % "9.4-1200-jdbc41" % "compile",
    "c3p0"                      %  "c3p0"            % "0.9.1.2" % "compile",
    "org.flywaydb"              %  "flyway-core"     % "3.1" % "compile",
    "org.slf4j"                 %  "slf4j-api"       % "1.7.10",
    "ch.qos.logback"            %  "logback-classic" % "1.1.2",
    "org.apache.httpcomponents" % "httpclient" % "4.3.3" % "test",
    "org.scalatest"             %% "scalatest"       % "2.2.5" % "test"
).map(_.withSources())
