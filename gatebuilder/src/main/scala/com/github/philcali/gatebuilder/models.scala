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

case class ResourceMethod(method: String)

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
