package com.github.philcali.awslambda

import collection.JavaConversions.{ mapAsScalaMap }
import unfiltered.request.HttpRequest
import unfiltered.response.HttpResponse

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, BufferedReader, InputStreamReader }

class RequestBinding(req: RequestObject) extends HttpRequest(req) {
  def context = req.getContext
  def method = req.getMethod
  def headerNames = req.getHeaders.keysIterator
  def headers(name: String) = mapAsScalaMap(req.getHeaders)
    .get(name)
    .map(Iterator(_))
    .getOrElse(Iterator.empty)
  def parameterNames = req.getQueryParams.keysIterator
  def parameterValues(param: String) = mapAsScalaMap(req.getQueryParams)
    .get(param)
    .map(Seq(_))
    .getOrElse(Nil)
  def inputStream = new ByteArrayInputStream(req.getBytes.orElse(Array.empty))
  def reader = new BufferedReader(new InputStreamReader(inputStream, req.getCharset))
  def protocol = "https"
  def isSecure = true
  def remoteAddr = req.getSourceIp
  lazy val uri = {
    val resource = req.getPathParams.foldLeft(req.getResourcePath)({
      case (path, (k, v)) => path.replace("{" + k + "}", v)
    })
    req.getQueryParams.isEmpty match {
      case true => resource
      case false => req.getQueryParams.foldLeft(resource + "?queryString=1")({
        case (path, (k, v)) => path + "&" + k + "=" + v
      })
    }
  }
}

class ResponseObject {
  var status: Int = 200
  var raw: Boolean = false
  var redirect: Option[String] = None
  val headers: collection.mutable.Map[String, String] = collection.mutable.Map.empty
  val outputStream = new ByteArrayOutputStream()
}

class ResponseBinding(resp: ResponseObject) extends HttpResponse(resp) {
  def status = resp.status
  def status(statusCode: Int) = resp.status = statusCode
  def redirect(url: String) = resp.redirect = Some(url)
  def header(name: String, value: String) = resp.headers += (name -> value)
  def outputStream = resp.outputStream
  def forward(raw: Boolean) = resp.raw = raw
}
