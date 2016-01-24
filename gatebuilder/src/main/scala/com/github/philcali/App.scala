package com.github.philcali.gatebuilder

import models._
import io.Source.{ fromFile => open }

import java.nio.file.Paths
import java.nio.file.Files

object Application extends App {
  import argonaut._, Argonaut._

  val converter = DefaultApiConverter(ConversionInput.fromArgs(args))
  converter.input.source.foreach({
    case file =>
    val json = open(args(0)).getLines.mkString("\n")
    Parse.decodeEither(json)(ApiCodecJson).fold(
      println _, converter.apply)
  })
}
