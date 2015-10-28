import android.Keys._

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

android.Plugin.androidBuild

name := "kitlog-android-client"

scalaVersion := "2.11.7"

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize", "-keepattributes Signature", "-printseeds target/seeds.txt", "-printusage target/usage.txt"
  , "-dontwarn scala.collection.**", "-dontwarn javax.inject.**", "-dontwarn org.w3c.dom.bootstrap.**", "-dontwarn scala.xml.**"
)

apkbuildExcludes in Android += "META-INF/LICENSE"
apkbuildExcludes in Android += "META-INF/NOTICE"

libraryDependencies ++= Seq(
  "org.scaloid" %% "scaloid" % "3.6.1-10",
  "clientapi" %% "clientapi" % "0.1-SNAPSHOT",
//  aar("com.android.support" % "appcompat-v7" % "23.0.1"),
  aar("com.android.support" % "support-v4" % "23.0.1"),
  aar("com.android.support" % "recyclerview-v7" % "23.0.1")
).map(_.withSources())

//libraryDependencies ++= Seq(
//  aar("com.google.android.gms" % "play-services" % "8.1.0")
//)

//libraryDependencies ++= Seq(
//  aar("com.android.support" % "design" % "23.0.1")
//)

scalacOptions in Compile += "-feature"

run <<= run in Android

install <<= install in Android
