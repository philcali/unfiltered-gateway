package com.github.philcali.awslambda

import collection.JavaConversions.{ mapAsScalaMap }
import unfiltered.request.HttpRequest
import unfiltered.response.HttpResponse

import java.io.{ ByteArrayInputStream, BufferedReader, InputStreamReader }

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

class ResponseBinding(resp: ResponseObject) extends HttpResponse(resp) {
  def status = resp.getStatus()
  def status(statusCode: Int) = resp.setStatus(statusCode)
  def redirect(url: String) = {
    resp.setStatus(304)
    header("Location", url)
  }
  def header(name: String, value: String) = resp.getHeaders().put(name, value)
  def outputStream = resp.getOutputStream()
  def forward(raw: Boolean) = resp.setRaw(raw)
}

case class InvalidResponseException(response: ResponseObject)
  extends RuntimeException(s"${response.getStatus}: ${new String(response.getOutputStream.toByteArray())}")
