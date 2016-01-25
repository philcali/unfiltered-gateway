package com.github.philcali.gatebuilder

import models._
import collection.JavaConversions._
import language.reflectiveCalls
import util.Try

import java.io.File
import java.nio.ByteBuffer
import java.nio.file.{ Files, Paths, StandardOpenOption }
import java.util.Collections

import com.amazonaws.services.apigateway.AmazonApiGateway
import com.amazonaws.services.apigateway.AmazonApiGatewayClient
import com.amazonaws.services.apigateway.model._
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient
import com.amazonaws.services.identitymanagement.model._
import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model._
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._

case class DefaultApiConverter(input: ConversionInput) extends RestConverter {
  val apiClient = new AmazonApiGatewayClient()
  val lambdaClient = new AWSLambdaClient()
  val iamClient = new AmazonIdentityManagementClient()
  val s3Client = new AmazonS3Client()
}


trait ResourceConverter {
  self: ModelConverter with ArtifactConverter =>

  def updateMethod(api: Api, resource: Resource, method: ResourceMethod)(implicit definitions: Map[String, ArtifactDefinition]) = resource.getResourceMethods().containsKey(method.method) match {
    case true =>
    case false =>
    val putMethod = new PutMethodRequest()
      .withRestApiId(api.id.get)
      .withResourceId(resource.getId())
      .withHttpMethod(method.method)
      .withAuthorizationType("NONE")
      .withApiKeyRequired(false)
    method.request.client.models.foreach({
      case (contentType, modelName) =>
      putMethod.addRequestModelsEntry(contentType, modelName)
    })
    method.request.client.parameters.foreach({
      case (location, params) =>
      params.foreach({
        case (name, value) =>
        putMethod.addRequestParametersEntry(s"method.request.${location}.${name}", value)
      })
    })
    apiClient.putMethod(putMethod)
    val putIntegration = new PutIntegrationRequest()
      .withRestApiId(api.id.get)
      .withResourceId(resource.getId())
      .withHttpMethod(method.method)
      .withIntegrationHttpMethod(method.method)
    method.request.integration.templates.foreach({
      case (contentType, template) =>
      putIntegration.addRequestTemplatesEntry(contentType, method.request.integration.template(contentType))
    })
    method.request.integration.parameters.foreach({
      case (location, params) =>
      params.foreach({
        case (name, value) =>
        putIntegration.addRequestParametersEntry(s"integration.request.${location}.${name}", value)
      })
    })
    method.request.integration.artifact.foreach({
      case artifact =>
      putIntegration
        .withType(IntegrationType.AWS)
        .withUri(method.request.integration.uri(definitions(artifact).id.get))
    })
    apiClient.putIntegration(putIntegration)
    method.response.foreach({
      case (code, response) =>
      val putMethodResponse = new PutMethodResponseRequest()
        .withRestApiId(api.id.get)
        .withResourceId(resource.getId())
        .withHttpMethod(method.method)
        .withStatusCode(code)
      response.client.parameters.foreach({
        case (location, params) =>
        params.foreach({
          case (name, value) =>
          putMethodResponse.addResponseParametersEntry(s"method.response.${location}.${name}", value)
        })
      })
      response.client.models.foreach({
        case (contentType, model) =>
        putMethodResponse.addResponseModelsEntry(contentType, model)
      })
      apiClient.putMethodResponse(putMethodResponse)
      val putIntegrationResponse = new PutIntegrationResponseRequest()
        .withRestApiId(api.id.get)
        .withResourceId(resource.getId())
        .withHttpMethod(method.method)
        .withStatusCode(code)
      response.integration.parameters.foreach({
        case (location, params) =>
        params.foreach({
          case (name, value) =>
          putIntegrationResponse.addResponseParametersEntry(s"integration.response.${location}.${name}", value)
        })
      })
      putIntegrationResponse.withSelectionPattern(response.integration.selection)
      response.integration.templates.foreach({
        case (contentType, template) =>
        putIntegrationResponse.addResponseTemplatesEntry(contentType, response.integration.template(contentType))
      })
      apiClient.putIntegrationResponse(putIntegrationResponse)
    })
  }

  def updateResources(api: Api) = {
    implicit val definitions = updateArtifacts(api)
    val resources = apiClient.getResources(new GetResourcesRequest().withRestApiId(api.id.get)).getItems.map({
      case resource => resource.getPath() -> resource
    }).toMap
    def update(resource: ResourceMethods, parent: Option[Resource] = None) {
      val restResource = resources
        .get(parent.foldRight(resource.name)(_.getPath().stripSuffix("/") + "/" + _))
        .orElse({
          val request = new CreateResourceRequest().withPathPart(resource.name)
          api.id.foreach(request.withRestApiId)
          parent.map(_.getId).foreach(request.withParentId)
          Try(apiClient.createResource(request))
            .map({
              case result => new Resource()
                .withPath(result.getPath())
                .withId(result.getId())
                .withResourceMethods(result.getResourceMethods())
                .withParentId(result.getParentId())
                .withPathPart(result.getPathPart())
            })
            .toOption
        })
        .map(r =>{
          if (r.getResourceMethods() == null) {
            r.withResourceMethods(Collections.emptyMap())
          }
          r
        })
      resource.methods.foreach(updateMethod(api, restResource.get, _))
      resource.resources.foreach(update(_, restResource))
    }
    api.resources.map(update(_, None))
  }
}

trait ModelConverter {
  val apiClient: AmazonApiGateway

  def updateModels(api: Api) = {
    val getRequest = new GetModelsRequest()
    api.id.foreach(getRequest.withRestApiId)
    val models = Map(apiClient.getModels(getRequest)
      .getItems
      .map(model => model.getName() -> model):_*)
    api.models.foreach({
      case model => models.get(model.name).orElse({
        val request = new CreateModelRequest()
          .withName(model.name)
          .withSchema(model.schema)
          .withContentType(model.contentType)
        model.description.foreach(request.withDescription)
        api.id.foreach(request.withRestApiId)
        Try(apiClient.createModel(request)).toOption
      })
    })
    api
  }
}

trait ArtifactConverter {
  type FunctionalCode = {
    def withZipFile(buffer: ByteBuffer): Any
    def withS3Bucket(bucket: String): Any
    def withS3Key(key: String): Any
    def withS3ObjectVersion(versionId: String): Any
  }

  val lambdaClient: AWSLambda
  val iamClient: AmazonIdentityManagement
  val s3Client: AmazonS3
  def input(): ConversionInput

  lazy val functions = Map(lambdaClient.listFunctions.getFunctions.map({
    case function => function.getFunctionName() -> function
  }):_*)

  def updateArtifacts(api: Api) = api.artifacts.map({
    case Artifact(functionType, LamdaArtifactDefinition(name, role, file, handler, s3, _)) =>
    Try(iamClient.getRole(new GetRoleRequest().withRoleName(role)))
      .map(_.getRole())
      .map({
        case iamRole =>
        def uploadCode(code: FunctionalCode): Unit = s3 match {
          case Some(S3Config(bucket, prefix)) =>
          val s3Bucket = input.bucket.getOrElse(bucket)
          val s3File = new File(input.zipFile.getOrElse(file))
          val s3Key = input.prefix.getOrElse(prefix.getOrElse(api.name)) + "/" + s3File.getName()
          s3Client.putObject(new PutObjectRequest(s3Bucket, s3Key, s3File)) match {
            case result =>
            code.withS3Bucket(s3Bucket)
            code.withS3Key(s3Key)
            code.withS3ObjectVersion(result.getVersionId())
          }
          case None =>
          val path = Paths.get(input.zipFile.getOrElse(file))
          val channel = Files.newByteChannel(path, StandardOpenOption.READ)
          val buffer = ByteBuffer.allocate(Files.size(path).toInt)
          code.withZipFile(buffer)
        }
        functions.get(name)
          .map({
            case function if input.updateArtifacts =>
            val request = new UpdateFunctionCodeRequest()
            uploadCode(request
              .withFunctionName(function.getFunctionName())
              .withPublish(true))
            Try(lambdaClient.updateFunctionCode(request))
            .map({
              case result => LamdaArtifactDefinition(
                name,
                role,
                input.zipFile.getOrElse(file),
                input.mainClass.getOrElse(handler),
                s3,
                Some(result.getFunctionArn()))
            })
            .get
            case function => LamdaArtifactDefinition(name, role, file, handler, s3, Some(function.getFunctionArn()))
          })
          .orElse({
            val code = new FunctionCode()
            uploadCode(code)
            Some(Try(lambdaClient.createFunction(new CreateFunctionRequest()
              .withCode(code)
              .withPublish(true)
              .withHandler(input.mainClass.getOrElse(handler))
              .withDescription("Created with gatebuilder")
              .withMemorySize(512)
              .withTimeout(60)
              .withRuntime(Runtime.Java8)
              .withRole(iamRole.getArn())
              .withFunctionName(name)))
            .map({
              case result => LamdaArtifactDefinition(
                name,
                role,
                input.zipFile.getOrElse(file),
                input.mainClass.getOrElse(handler),
                s3,
                Some(result.getFunctionArn()))
            })
            .get)
          })
          .get
      })
      .get
  }).map({
    case definition => definition.name -> definition
  }).toMap
}

trait ApiConverter {
  self: ModelConverter =>

  def updateApi(api: Api) = apiClient
    .getRestApis(new GetRestApisRequest()).getItems
    .find(_.getName == api.name)
    .map({
      case rest =>
      val request = new UpdateRestApiRequest()
        .withRestApiId(rest.getId())
        .withPatchOperations(
          new PatchOperation()
            .withOp(Op.Replace)
            .withPath("/name")
            .withValue(api.name))
      api.description.foreach(description => {
        request.withPatchOperations(new PatchOperation()
          .withOp(Op.Replace)
          .withPath("/description")
          .withValue(description))
      })
      apiClient.updateRestApi(request)
    })
    .map(rest => api.copy(id = Some(rest.getId())))
    .orElse({
      val request = new CreateRestApiRequest().withName(api.name)
      api.description.foreach(request.withDescription)
      Try(apiClient.createRestApi(request))
        .map({
          case result => api.copy(id = Some(result.getId()))
        }).toOption
    })
}

trait RestConverter
  extends ModelConverter
  with ApiConverter
  with ArtifactConverter
  with ResourceConverter {

  def apply(api: Api) {
    updateApi(api).map(updateModels).map(updateResources)
  }
}

object ConversionInput {
  def fromArgs(args: Array[String]) = new DirectConversionInput(args:_*)
}

trait ConversionInput {
  val mainClass: Option[String]
  val zipFile: Option[String]
  val source: Option[String]
  val bucket: Option[String]
  val prefix: Option[String]
  val updateArtifacts: Boolean
}

class DirectConversionInput(args: String*) extends ConversionInput {
  private lazy val argMap = Map(args.map({
    case arg if arg contains "=" => arg.replaceAll("^-*", "").split("=") match {
      case Array(key, value) => key -> value
    }
    case arg if arg startsWith "-" => arg.replaceAll("^-*", "") -> "true"
    case arg => "source" -> arg
  }):_*)
  lazy val mainClass = argMap.get("mainClass")
  lazy val zipFile = argMap.get("zipFile")
  lazy val source = argMap.get("source")
  lazy val bucket = argMap.get("s3Bucket")
  lazy val prefix = argMap.get("s3Prefix")
  lazy val updateArtifacts = argMap.get("artifacts").map(_.toBoolean).getOrElse(false)

  override def toString() = s"DirectConversionInput(${mainClass},${zipFile},${source},${bucket},${prefix})"
}
