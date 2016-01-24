package com.github.philcali

import awslambda._

class Example extends EmbeddedApp with LambdaExecution {
  def server = Server.plan(Planify(Example.intent))
}

object Example {
  import unfiltered.request._
  import unfiltered.response._
  import unfiltered.directives._, Directives._

  def intent = Directive.Intent.Path {
    case "/" => for (_ <- GET) yield (ResponseString("Hello World!"))
  }
}
