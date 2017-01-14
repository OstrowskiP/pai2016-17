name := "WindowsRetail-PAI-Server"

version := "1.0"

scalaVersion := "2.11.8"

lazy val dependencies = Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1",
  "org.json4s" %% "json4s-native" % "3.4.0",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "com.typesafe" % "config" % "1.2.1",
  "org.apache.logging.log4j" % "log4j-api" % "2.6.2",
  "org.apache.logging.log4j" % "log4j-core" % "2.6.2",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.6.2",
  "org.apache.httpcomponents" % "httpclient" % "4.5.2"
)

lazy val dependenciesAkka = Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.8",
  "com.typesafe.akka" %% "akka-stream" % "2.4.8",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.8",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.8",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.8"
)

libraryDependencies ++= dependencies
libraryDependencies ++= dependenciesAkka

scalacOptions ++= Seq("-feature", "-language:postfixOps")