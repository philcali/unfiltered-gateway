name := "awslambda"

organization := "com.github.philcali"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered" % "0.8.4",
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-log4j" % "1.0.0"
)
