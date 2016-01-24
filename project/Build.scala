import sbt._
import Keys._

object Build extends sbt.Build {
  override lazy val settings = super.settings ++ Seq(
    organization := "com.github.philcali",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-feature", "-deprecation")
  )

  lazy val root = Project(
    "awslambda",
    file(".")
  ) aggregate (unfiltered, gatebuilder, example)

  lazy val example = Project(
    "awslambda-example",
    file("example"),
    settings = Seq(
      libraryDependencies += "net.databinder" %% "unfiltered-directives" % "0.8.4"
    )
  ) dependsOn unfiltered

  lazy val unfiltered = Project(
    "awslambda-unfiltered",
    file("unfiltered"),
    settings = Seq(
      libraryDependencies ++= Seq(
        "net.databinder" %% "unfiltered" % "0.8.4",
        "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
        "com.amazonaws" % "aws-lambda-java-events" % "1.1.0",
        "com.amazonaws" % "aws-lambda-java-log4j" % "1.0.0"
      )
    )
  )

  lazy val gatebuilder = Project(
    "awslambda-gatebuilder",
    file("gatebuilder"),
    settings = Seq(
      libraryDependencies ++= Seq(
        "com.amazonaws" % "aws-java-sdk-api-gateway" % "1.10.47",
        "com.amazonaws" % "aws-java-sdk-iam" % "1.10.47",
        "com.amazonaws" % "aws-java-sdk-lambda" % "1.10.47",
        "com.amazonaws" % "aws-java-sdk-s3" % "1.10.47",
        "io.argonaut" %% "argonaut" % "6.0.4"
      )
    )
  )
}
