import android.Keys._

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

android.Plugin.androidBuild

name := "kitlog-android-client"

scalaVersion := "2.11.4"

proguardCache in Android ++= Seq(
  ProguardCache("org.scaloid") % "org.scaloid"
)

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize", "-keepattributes Signature", "-printseeds target/seeds.txt", "-printusage target/usage.txt"
  , "-dontwarn scala.collection.**" // required from Scala 2.11.4
)

libraryDependencies ++= Seq(
    "org.scaloid" %% "scaloid" % "3.6.1-10" withSources() withJavadoc(),
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2" withSources() withJavadoc(),
    "net.databinder.dispatch" %% "dispatch-json4s-jackson" % "0.11.2" withSources() withJavadoc()
)

scalacOptions in Compile += "-feature"

run <<= run in Android

install <<= install in Android

