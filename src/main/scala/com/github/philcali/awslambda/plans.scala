package com.github.philcali.awslambda

import collection.JavaConversions.{ mapAsScalaMap }
import unfiltered.Cycle
import unfiltered.request.HttpRequest
import unfiltered.response.{ HttpResponse, Pass, ResponseFunction }

import java.io.{ InputStream, OutputStream }
import java.util.{ Map => JMap, HashMap => JHashMap }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler };
import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.fasterxml.jackson.core.`type`.TypeReference

trait PlanChain {
  def doPlan(request: RequestObject, response: ResponseObject)
}

case class RecursePlanChain(plans: Iterator[Plan]) extends PlanChain {
  def doPlan(request: RequestObject, response: ResponseObject) {
    if (plans.hasNext) {
      plans.next.doPlan(request, response, this)
    }
  }
}

object Plan {
  type Intent = Cycle.Intent[RequestObject, ResponseObject]
}

object Intent {
  def apply(intent: Plan.Intent) = intent
}

trait InittedFunction extends RequestStreamHandler {
  def doPlan(req: RequestObject, resp: ResponseObject, chain: PlanChain)
  def defaulPlanChain: PlanChain
  def defaultResponse = new ResponseObject
  def newMapper = new ObjectMapper
  def withMapper(mapper: ObjectMapper)(thunk: ObjectMapper => Unit) = {
    thunk(mapper)
    mapper
  }
  def toBody(bytes: Array[Byte], mapper: ObjectMapper): JMap[String, Object] = {
    val map = new JHashMap[String, Object]()
    try {
      val node = mapper.readTree(bytes)
      map.put("value", node)
    } catch {
      case _: Throwable =>
      map.put("value", new String(bytes))
    }
    map
  }
  override def handleRequest(input: InputStream, output: OutputStream, context: Context) {
    withMapper(newMapper) { mapper =>
      val request = mapper.readValue(input, classOf[RequestObject])
      request.setContext(context)
      if (request.getBody().containsKey("value")) {
        try {
          request.setBytes(mapper.writeValueAsBytes(request.getBody().get("value")))
        } catch {
          case _: Throwable =>
          request.setBytes(request.getBody().get("value").toString().getBytes())
        }
      }
      request.getEnvironment.foreach({
        case (name, value) =>
        java.lang.System.setProperty("stageVariable." + name, value)
      })
      val response = defaultResponse
      doPlan(request, response, defaulPlanChain)
      if (response.raw) {
        output.write(response.outputStream.toByteArray())
      } else {
        mapper.writeValue(output, new MessageObject()
          .withCode(response.status)
          .withBody(toBody(response.outputStream.toByteArray(), mapper)))
      }
    }
  }
}

trait Plan extends InittedFunction {
  def intent: Plan.Intent
  def defaulPlanChain = RecursePlanChain(Seq(this).iterator)
  def doPlan(req: RequestObject, resp: ResponseObject, chain: PlanChain) {
    val request = new RequestBinding(req)
    val response = new ResponseBinding(resp)
    Pass.fold(
      intent,
      (_: HttpRequest[RequestObject]) =>
      chain.doPlan(request.underlying, response.underlying),
      (_: HttpRequest[RequestObject], rf: ResponseFunction[ResponseObject]) => {
        rf(response)
      })(request)
  }
}

object Planify {
  def apply(intentIn: Plan.Intent) = new Plan {
    val intent = intentIn
  }
}
