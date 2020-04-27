organization := "com.bnd-lib"

name := "scala-basecamp-3-client"

version := "0.0.3"

description := "Scala client for Basecamp 3 API implemented using Play WS lib."

isSnapshot := false

scalaVersion := "2.11.12" // or "2.12.10"


resolvers ++= Seq(
  Resolver.mavenLocal
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ws" % "2.5.9" exclude("commons-logging", "commons-logging"), // WS
  "net.codingwell" %% "scala-guice" % "4.0.1",                                              // Guice
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",                                // Logging
//  "ch.qos.logback" % "logback-classic" % "1.2.3",                                         // Logging
  "net.codingwell" %% "scala-guice" % "4.0.1"
)

// POM settings for Sonatype
homepage := Some(url("https://github.com/peterbanda/scala-basecamp-3-client"))

publishMavenStyle := true

scmInfo := Some(ScmInfo(url("https://github.com/peterbanda/scala-basecamp-3-client"), "scm:git@github.com:peterbanda/scala-basecamp-3-client.git"))

developers := List(
  Developer("bnd", "Peter Banda", "peter.banda@protonmail.com", url("https://peterbanda.net"))
)

licenses += "Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")

publishMavenStyle := true

// publishTo := sonatypePublishTo.value

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)