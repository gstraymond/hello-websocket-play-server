name := """echo-websocket-play-server"""

version := "0.0.1"

lazy val `hello-websocket-play-server` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)