package com.bnd.basecampclient.service

import java.util.concurrent.TimeoutException

import com.bnd.basecampclient.model.{Person, Project, Upload}
import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import javax.annotation.PreDestroy
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.libs.ws._
import play.api.libs.ws.ning._
import Project.format
import Person.format
import Upload.format
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.bnd.basecampclient.{Basecamp3Exception, Basecamp3UnauthorizedException}
import org.asynchttpclient.AsyncHttpClientConfig
import play.api.Logger
import play.api.libs.ws.ahc.{AhcConfigBuilder, AhcWSClient, AhcWSClientConfig}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[Basecamp3ServiceImpl])
trait Basecamp3Service {

  def projects(accountId: Int): Future[Seq[Project]]

  def people(accountId: Int): Future[Seq[Person]]

  def uploads(accountId: Int, bucket: Long, vault: Long): Future[Seq[Upload]]

  def downloadFile(accountId: Int, bucket: Long, upload: Long, fileName: String): Future[ByteString]

  def downloadFileStreamed(accountId: Int, bucket: Long, upload: Long, fileName: String): Future[Source[ByteString, _]]

  def close: Unit
}

@Singleton
protected class Basecamp3ServiceImpl @Inject()(config: Config) extends Basecamp3Service {

  private val logger = Logger

  private val token = config.getString("basecamp3.token")

  private val requestTimeout = 2000

  private val coreUrl = "https://3.basecampapi.com/"

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private object EndPoint extends Enumeration {
    val projects = Value("projects.json")
    val people = Value("people.json")
    def uploads(bucket: Long, vault: Long) = Value(s"buckets/$bucket/vaults/$vault/uploads.json")
    def download(bucket: Long, upload: Long, fileName: String) = Value(s"buckets/$bucket/uploads/$upload/download/$fileName")
  }

  private val client: WSClient = {
    val config = new AhcConfigBuilder(AhcWSClientConfig()).build()
    new AhcWSClient(config)
  }

  override def projects(accountId: Int) =
    getRequest[Project](EndPoint.projects, accountId)

  override def people(accountId: Int) =
    getRequest[Person](EndPoint.people, accountId)

  override def uploads(
    accountId: Int,
    bucket: Long,
    vault: Long
  ) =
    getRequest[Upload](EndPoint.uploads(bucket, vault), accountId)

  override def downloadFile(
    accountId: Int,
    bucket: Long,
    upload: Long,
    fileName: String
  ) =
    for {
      rawDowloadResponse <- getRequestRaw(
        url(EndPoint.download(bucket, upload, fileName), accountId),
        true
      )

      locationToFollow = rawDowloadResponse.header("Location").getOrElse(
        throw new Basecamp3Exception(s"Location to follow to download a filew $fileName not found.")
      )

      fileResponse <- getRequestRaw(locationToFollow, false)
    } yield {
      if (fileResponse.status == 200) {
        fileResponse.bodyAsBytes
      } else
        throw new Basecamp3Exception(s"File cannot be dowloaded due to ${fileResponse.status} : ${fileResponse.statusText})")
    }

  override def downloadFileStreamed(
    accountId: Int,
    bucket: Long,
    upload: Long,
    fileName: String
  ) =
    for {
      rawDowloadResponse <- getRequestRaw(
        url(EndPoint.download(bucket, upload, fileName), accountId),
        true
      )

      locationToFollow = rawDowloadResponse.header("Location").getOrElse(
        throw new Basecamp3Exception(s"Location to follow to download a filew $fileName not found.")
      )

      fileResponse <- getRequestRawStreamed(locationToFollow, false)
    } yield {
      if (fileResponse.headers.status == 200) {
        fileResponse.body
      } else
        throw new Basecamp3Exception(s"File cannot be dowloaded due to ${fileResponse.headers.status}.)")
    }

  /////////////////
  // Helper funs //
  /////////////////

  private def getRequest[T: Reads](
    endPoint: EndPoint.Value,
    accountId: Int,
    withAuthorization: Boolean = true
  ): Future[Seq[T]] =
    getRequestRaw(url(endPoint, accountId), withAuthorization).map { response =>
      handleErrorResponse(response)
      response.json match {
        case array: JsArray =>
          array.value.map( json =>
            json.validate[T] match {
              case jsSuccess: JsSuccess[T] =>
                jsSuccess.value
              case jsError: JsError =>
                logger.error(s"Got a problem: ''${jsError.toString}'' while parsing a JSON: ${Json.prettyPrint(json)}.")
                throw new Basecamp3Exception(s"Got a problem: ''${jsError.toString}'' while parsing a JSON: ${Json.prettyPrint(json)}.")
            }
          )

        case _ => throw new Basecamp3Exception(s"JSON array response expected but got ${response.body}.")
      }
    }.recover {
      case e: TimeoutException => throw new Basecamp3Exception(s"Basecamp3Service.$endPoint timed out: ${e.getMessage}.")
    }

  private def getRequestRaw(
    url: String,
    withAuthorization: Boolean
  ): Future[WSResponse] = {
    val request = requestRawAux(url, withAuthorization)
    request.get
  }

  private def getRequestRawStreamed(
    url: String,
    withAuthorization: Boolean
  ) : Future[StreamedResponse] = {
    val request = requestRawAux(url, withAuthorization)
    request.stream
  }

  private def requestRawAux(
    url: String,
    withAuthorization: Boolean
  ): WSRequest = {
    val request = client.url(url)
      .withFollowRedirects(false)
      .withRequestTimeout(requestTimeout millis)

    if (withAuthorization)
      request.withHeaders("Authorization" -> s"Bearer $token")
    else
      request
  }

//
//    val bytesReturned: Future[Long] = futureResponse.flatMap {
//      res =>
//        // Count the number of bytes returned
//        res.body.runWith(Sink.fold[Long, ByteString](0L){ (total, bytes) =>
//          total + bytes.length
//        })

  private val handleErrorResponse: WSResponse => Unit = { response =>
    response.status match {
      case x if x >= 200 && x <= 299 => ()
      case 401 | 403 => throw new Basecamp3UnauthorizedException(response.status + ": Unauthorized access.")
      case _ => throw new Basecamp3Exception(response.status + ": " + response.statusText + "; " + response.body)
    }
  }

  private def url(
    endpoint: EndPoint.Value,
    accountId: Int
  ) =
    coreUrl + accountId + "/" + endpoint.toString

  @PreDestroy
  override def close = {
    println("Closing Basecamp connection")
    client.close
  }
}