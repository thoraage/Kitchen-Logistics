import sbt._
//import com.github.siasia._
//import WebPlugin._
//import PluginKeys._
import Keys._

object Build extends sbt.Build {
  import Dependencies._

  lazy val myProject = Project("kitchen-logistics", file("."))
//    .settings(WebPlugin.webSettings: _*)
//    .settings(port in config("container") := 8080)
    .settings(
      organization  := "com.example",
      version       := "0.9.0",
      scalaVersion  := "2.10.2",
      scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
      libraryDependencies ++= Seq(
        "net.databinder"            %% "unfiltered-directives" % V.unfiltered,
        "net.databinder"            %% "unfiltered-jetty" % V.unfiltered,
        "net.databinder"            %% "unfiltered-filter" % V.unfiltered,
        "net.databinder"            %% "unfiltered-directives" % V.unfiltered,
        "net.databinder"            %% "unfiltered-spec" % V.unfiltered % "test",
        "javax.servlet"             % "servlet-api"      % "2.3" % "provided",
//        "org.eclipse.jetty"         % "jetty-webapp"     % "7.4.5.v20110725" % "container",
    //    "org.scalaquery"            %% "scalaquery"      % "0.10.0-M1" % "compile",
        "com.h2database"            %  "h2"              % "1.3.166" % "compile",
        "c3p0"                      %  "c3p0"            % "0.9.1.2" % "compile",
        "org.specs2"                %% "specs2"          % V.specs2  % "test",
  //      "org.eclipse.jetty"         %  "jetty-webapp"    % V.jetty   % "container",
        "org.slf4j"                 %  "slf4j-api"       % V.slf4j,
        "ch.qos.logback"            %  "logback-classic" % V.logback
      )
    )
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