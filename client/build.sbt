import android.Keys._

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

android.Plugin.androidBuild

name := "kitlog-android-client"

scalaVersion := "2.11.5"

proguardCache in Android ++= Seq(
  ProguardCache("org.scaloid") % "org.scaloid"
)

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize", "-keepattributes Signature", "-printseeds target/seeds.txt", "-printusage target/usage.txt"
  , "-dontwarn scala.collection.**", "-dontwarn javax.inject.**", "-dontwarn org.w3c.dom.bootstrap.**", "-dontwarn scala.xml.**"
)

apkbuildExcludes in Android += "META-INF/LICENSE"

apkbuildExcludes in Android += "META-INF/NOTICE"

libraryDependencies ++= Seq(
    "org.scaloid" %% "scaloid" % "3.6.1-10",
//    "net.databinder" %% "dispatch-http" % "0.8.10" withSources() withJavadoc()
//        excludeAll(ExclusionRule(organization = "org.apache.httpcomponents"), ExclusionRule(organization = "org.scala-lang"),
//        ExclusionRule(organization = "org.scala-lang.modules"), ExclusionRule(name = "dispatch-futures_2.11"))
//    "net.databinder" %% "dispatch-http-json" % "0.8.10" withSources() withJavadoc(),
//    "org.json4s" %% "json4s-core" % "3.2.11" withSources() withJavadoc()
//        excludeAll(ExclusionRule(organization = "org.scala-lang"), ExclusionRule(organization = "org.scala-library")),
//    "org.json4s" %% "json4s-jackson" % "3.3.0-SNAPSHOT"
//        excludeAll(ExclusionRule(organization = "org.scala-lang"), ExclusionRule(organization = "org.scala-library"))
    "io.argonaut" %% "argonaut" % "6.0.4"
).map(_.withSources())

scalacOptions in Compile += "-feature"

run <<= run in Android

install <<= install in Android
