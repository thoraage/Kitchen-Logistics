import NativePackagerKeys._

packageArchetype.java_application

name := "kitlog"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
    "net.databinder"            %% "unfiltered-directives" % "0.7.0",
    "net.databinder"            %% "unfiltered-jetty" % "0.7.0",
    "net.databinder"            %% "unfiltered-filter" % "0.7.0",
    "net.databinder"            %% "unfiltered-directives" % "0.7.0",
    "net.databinder"            %% "unfiltered-json4s" % "0.7.0",
    "net.databinder"            %% "unfiltered-spec" % "0.7.0" % "test",
    "com.typesafe.slick"        %% "slick" % "1.0.1",
    "com.github.tototoshi"      %% "slick-joda-mapper" % "0.4.0",
    "javax.servlet"             % "servlet-api"      % "2.3" % "provided",
    "com.h2database"            %  "h2"              % "1.3.166" % "compile",
    "org.postgresql"            % "postgresql"       % "9.4-1200-jdbc41" % "compile",
    "c3p0"                      %  "c3p0"            % "0.9.1.2" % "compile",
    "org.flywaydb" % "flyway-core" % "3.1" % "compile",
    "org.specs2"                %% "specs2"          % "2.2"  % "test",
    "org.slf4j"                 %  "slf4j-api"       % "1.6.4",
    "ch.qos.logback"            %  "logback-classic" % "1.0.0"
)
