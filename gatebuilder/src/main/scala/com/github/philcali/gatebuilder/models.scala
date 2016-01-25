package com.github.philcali.gatebuilder.models

import com.amazonaws.services.lambda.model.FunctionConfiguration

trait IdentifiableDefinition {
  def name: String
  def id: Option[String]
}

trait ArtifactDefinition extends IdentifiableDefinition {
  def source: String
}

trait ResourceTree extends IdentifiableDefinition {
  def resources: List[ResourceMethods]
}

trait ResourceMethods extends ResourceTree {
  def methods: List[ResourceMethod]
}

case class Api(
  name: String,
  description: Option[String],
  artifacts: List[Artifact] = Nil,
  resources: List[ResourceMethods] = Nil,
  models: List[ApiModel] = Nil,
  id: Option[String] = None
) extends ResourceTree

case class Artifact(
  name: String,
  definition: ArtifactDefinition)

case class LamdaArtifactDefinition(
  name: String,
  role: String,
  source: String,
  handler: String,
  s3: Option[S3Config] = None,
  id: Option[String] = None
) extends ArtifactDefinition

case class ResourceDefinition(
  name: String,
  methods: List[ResourceMethod] = Nil,
  resources: List[ResourceMethods] = Nil,
  id: Option[String] = None
) extends ResourceMethods

case class ResourceMethod(
  method: String,
  request: MethodRequest,
  response: Map[String, MethodResponse]
)

case class MethodRequest(
  client: ClientRequest,
  integration: IntegrationRequest
)

case class MethodResponse(
  client: ClientResponse,
  integration: IntegrationResponse
)

case class ClientRequest(
  models: Map[String, String],
  parameters: Map[String, Map[String, Boolean]] = Map.empty
)

case class IntegrationRequest(
  templates: Map[String, String],
  region: String,
  parameters: Map[String, Map[String, String]] = Map.empty,
  artifact: Option[String] = None
) {
  val arnVersion = "2015-03-31"
  def uri(arn: String) = s"arn:aws:apigateway:${region}:lambda:path/${arnVersion}/functions/${arn}/invocations"
  def template(contentType: String) = templates.get(contentType) match {
    case Some(template) if template == "$default" => """#set($inputRoot = $input.path('$'))
{
  "resourcePath" : "$context.resourcePath",
  "method" : "$context.httpMethod",
  "body" : { "value": $input.json('$') },
  "sourceIp" : "$input.params().header.get('X-Forwarded-For')",
  "headerString": "$input.params().header",
  "queryString": "$input.params().querystring",
  "pathString": "$input.params().path",
  "environmentString": "$stageVariables"
}"""
    case Some(template) => template
    case None => ""
  }
}

case class ClientResponse(
  models: Map[String, String],
  parameters: Map[String, Map[String, Boolean]]
)

case class IntegrationResponse(
  selection: String,
  templates: Map[String, String],
  parameters: Map[String, Map[String, String]]
) {
  def template(contentType: String) = templates.get(contentType) match {
    case Some(template) if template == "$default" =>
    """#set($inputRoot = $input.path('$'))
$input.json('$.body.value')"""
    case Some(template) => template
    case None => ""
  }
}

case class ApiModel(
  name: String,
  contentType: String,
  schema: String,
  description: Option[String] = None,
  id: Option[String] = None
) extends IdentifiableDefinition

case class S3Config(
  bucket: String,
  prefix: Option[String])
