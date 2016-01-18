package com.github.philcali.awslambda.response

import com.github.philcali.awslambda.ResponseObject
import unfiltered.response.{ HttpResponse, Responder }

object ForwardRaw extends Responder[ResponseObject] {
  def respond(res: HttpResponse[ResponseObject]) {
    res.underlying.raw = true
  }
}
