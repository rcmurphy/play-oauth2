name := "play-oauth2"

organization := "me.rcmurphy"

licenses += ("Apache-2.0", url("http://opensource.org/licenses/apache-2.0"))

scalaVersion := "2.11.8"

val playLibraryVersion = "2.5.1"

libraryDependencies ++= Seq(
  "com.netaporter" %% "scala-uri" % "0.4.7",
  "com.typesafe.play" %% "play-ws" % playLibraryVersion % "provided"
)
