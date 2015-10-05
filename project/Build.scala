import sbt._
import Keys._

object Build extends Build {
    lazy val clientApi = project.in(file("client-api"))
    lazy val server = project.in(file("server")).dependsOn(clientApi % "compile->test")
    //lazy val client = project.in(file("client")).dependsOn(clientApi)
}

