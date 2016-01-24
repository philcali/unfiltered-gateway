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
      resources <- (resource --\ "resources").as[Option[List[ResourceMethods]]]
      methods <- (resource --\ "methods").as[Option[List[ResourceMethod]]]
    } yield (ResourceDefinition(name, methods.getOrElse(Nil), resources.getOrElse(Nil))))

  implicit def ResourceMethodJson: DecodeJson[ResourceMethod] =
    DecodeJson(method => for {
      name <- (method --\ "method").as[String]
    } yield (ResourceMethod(name)))
}
