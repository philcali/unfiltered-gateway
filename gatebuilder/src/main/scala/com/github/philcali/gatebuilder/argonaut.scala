package com.github.philcali.gatebuilder

package object models {
  import argonaut._, Argonaut._

  implicit def ApiCodecJson: DecodeJson[Api] =
    DecodeJson(api => for {
      name <- (api --\ "name").as[String]
      description <- (api --\ "description").as[Option[String]]
      artifacts <- (api --\ "artifacts").as[List[Artifact]]
      resources <- (api --\ "resources").as[List[ResourceMethods]]
      models <- (api --\ "models").as[List[ApiModel]]
    } yield (Api(name, description, artifacts, resources, models)))

  implicit def ApiModelCodecJson: DecodeJson[ApiModel] =
    DecodeJson(model => for {
      name <- (model --\ "name").as[String]
      description <- (model --\ "description").as[Option[String]]
      contentType <- (model --\ "type").as[String]
      schema <- (model --\ "schema").as[Json]
    } yield (ApiModel(name, contentType, schema.spaces4, description)))

  implicit def ArtifactJson: DecodeJson[Artifact] =
    DecodeJson(artifact => for {
      name <- (artifact --\ "type").as[String]
      definition <- (artifact --\ "definition").as[LamdaArtifactDefinition]
    } yield (Artifact(name, definition)))

  implicit def LamdbaDefinitionJson: DecodeJson[LamdaArtifactDefinition] =
    DecodeJson(lambda => for {
      name <- (lambda --\ "name").as[String]
      role <- (lambda --\ "role").as[String]
      source <- (lambda --\ "file").as[String]
      handler <- (lambda --\ "handler").as[String]
      s3 <- (lambda --\ "s3").as[Option[S3Config]]
    } yield (LamdaArtifactDefinition(name, role, source, handler, s3)))

  implicit def S3ConfigJson: DecodeJson[S3Config] =
    DecodeJson(s3 => for {
      bucket <- (s3 --\ "bucket").as[String]
      prefix <- (s3 --\ "prefix").as[Option[String]]
    } yield (S3Config(bucket, prefix)))

  implicit def ResourceDefinitionJson: DecodeJson[ResourceMethods] =
    DecodeJson(resource => for {
      name <- (resource --\ "path").as[String]
      methods <- (resource --\ "methods").as[List[ResourceMethod]]
      resources <- (resource --\ "resources").as[Option[List[ResourceMethods]]]
    } yield (ResourceDefinition(name, methods, resources.getOrElse(Nil))))

  implicit def ResourceMethodJson: DecodeJson[ResourceMethod] =
    DecodeJson(method => for {
      name <- (method --\ "method").as[String]
      request <- (method --\ "request").as[MethodRequest]
      response <- (method --\ "response").as[Map[String, MethodResponse]]
    } yield (ResourceMethod(name, request, response)))

  implicit def MethodRequestJson: DecodeJson[MethodRequest] =
    DecodeJson(request => for {
      client <- (request --\ "client").as[ClientRequest]
      integration <- (request --\ "integration").as[IntegrationRequest]
    } yield (MethodRequest(client, integration)))

  implicit def MethodResponseJson: DecodeJson[MethodResponse] =
    DecodeJson(response => for {
      client <- (response --\ "client").as[ClientResponse]
      integration <- (response --\ "integration").as[IntegrationResponse]
    } yield (MethodResponse(client, integration)))

  implicit def ClientRequestJson: DecodeJson[ClientRequest] =
    DecodeJson(request => for {
      models <- (request --\ "models").as[Map[String, String]]
      parameters <- (request --\ "parameters").as[Option[Map[String, Map[String, Boolean]]]]
    } yield (ClientRequest(models, parameters.getOrElse(Map.empty))))

  implicit def IntegrationRequestJson: DecodeJson[IntegrationRequest] =
    DecodeJson(request => for {
      templates <- (request --\ "templates").as[Map[String, String]]
      parameters <- (request --\ "parameters").as[Option[Map[String, Map[String, String]]]]
      region <- (request --\ "region").as[Option[String]]
      artifact <- (request --\ "artifact").as[Option[String]]
    } yield (IntegrationRequest(templates, region.getOrElse("us-east-1"), parameters.getOrElse(Map.empty), artifact)))

  implicit def ClientResponseJson: DecodeJson[ClientResponse] =
    DecodeJson(response => for {
      models <- (response --\ "models").as[Map[String, String]]
      parameters <- (response --\ "parameters").as[Option[Map[String, Map[String, Boolean]]]]
    } yield (ClientResponse(models, parameters.getOrElse(Map.empty))))

  implicit def IntegrationResponseJson: DecodeJson[IntegrationResponse] =
    DecodeJson(response => for {
      selection <- (response --\ "selection").as[Option[String]]
      templates <-(response --\ "templates").as[Map[String, String]]
      parameters <- (response --\ "parameters").as[Option[Map[String, Map[String, String]]]]
    } yield (IntegrationResponse(selection.getOrElse(""), templates, parameters.getOrElse(Map.empty))))
}
