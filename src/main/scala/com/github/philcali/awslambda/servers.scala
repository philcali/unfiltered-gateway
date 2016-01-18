package com.github.philcali.awslambda

import unfiltered.response.{ NotFound, ResponseString }
import unfiltered.util.PlanServer
import java.io.{ InputStream, OutputStream }
import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler };

case class Server(plans: List[Plan] = Nil) extends PlanServer[Plan] {
  type ServerBuilder = Server
  def makePlan(plan: => Plan) = copy(plans = plan :: plans)
}

object Server extends Server(List(
  Planify({
    case req =>
    NotFound ~>
    ResponseString("Resource could not be found.")
  })
))

trait EmbeddedApp {
  type Embedded <: PlanServer[_]

  def server: Embedded
  def run(start: Embedded => Unit) = start(server)
}

trait LambdaExecution extends InittedFunction {
  self: EmbeddedApp =>
  type Embedded = Server

  def defaulPlanChain = RecursePlanChain(server.plans.iterator)
  def doPlan(req: RequestObject, resp: ResponseObject, chain: PlanChain) = chain.doPlan(req, resp)
}
