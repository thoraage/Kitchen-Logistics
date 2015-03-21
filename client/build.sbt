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
    "io.argonaut" %% "argonaut" % "6.0.4"
).map(_.withSources())

scalacOptions in Compile += "-feature"

run <<= run in Android

install <<= install in Android
