package uk.gov.gds.common.http

import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.client.methods.{ HttpRequestBase, HttpGet, HttpPost, HttpUriRequest }
import org.apache.http.entity.StringEntity
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.{ BasicHeader, BasicNameValuePair }
import org.apache.http.util.{ CharArrayBuffer, EntityUtils }
import org.apache.http.impl.client.{ DefaultHttpClient, DefaultHttpRequestRetryHandler }
import org.apache.http.params.{ BasicHttpParams, HttpConnectionParams }
import org.apache.http.client.params.HttpClientParams
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.ssl.SSLSocketFactory
import uk.gov.gds.common.logging.Logging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import scala.collection.JavaConversions._
import uk.gov.gds.common.json.JsonSerializer._
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.auth.BasicScheme
import org.apache.commons.codec.binary.Base64
import uk.gov.gds.common.config.Config

abstract class ApacheHttpClient extends UrlEncoding with Logging {

  private lazy val schemeRegistry = new SchemeRegistry
  private lazy val connectionManager = new ThreadSafeClientConnManager(schemeRegistry)
  private lazy val httpClient = configureHttpClient()
  private lazy val cleanupThread = Executors.newScheduledThreadPool(1)

  startupConnectionCleanerThread()

  def get(path: String, params: Map[String, Any] = Map.empty) = {
    execute(new HttpGet(targetUrl(path, paramsToUrlParams(params))))
  }

  def getWithBearerToken(path: String, params: Map[String, Any] = Map.empty, token: String) =
    execute(setAuthorizationHeader(new HttpGet(targetUrl(path, paramsToUrlParams(params))), token))

  def getOptional(path: String, params: Map[String, Any] = Map.empty) = {
    executeOptional(new HttpGet(targetUrl(path, paramsToUrlParams(params))))
  }

  def getFromJson[A](url: String, params: String*)(implicit m: Manifest[A]) =
    fromJson[A](get(String.format(url, params.map(urlEncode(_)): _*)))

  def getWithJson(url: String, params: String*) = get(String.format(url, params.map(urlEncode(_)): _*))

  def post(path: String) = execute(new HttpPost(targetUrl(path)))

  def post(path: String, params: Map[String, String]) = {
    val postRequest = new HttpPost(targetUrl(path))

    postRequest.setEntity(
      new UrlEncodedFormEntity(params.map {
        case (k, v) => new BasicNameValuePair(k, v)
      }.toList, "UTF-8")
    )
    execute(postRequest)
  }

  def postJson(path: String, json: String) = {
    val postRequest = new HttpPost(targetUrl(path))
    postRequest.setEntity(jsonToPostOverWire(json))

    execute(postRequest)
  }

  def postJson(path: String, jsonParams: Map[String, String]) = {
    val postRequest = new HttpPost(targetUrl(path))

    postRequest.setEntity(jsonToPostOverWire(jsonParams))
    execute(postRequest)
  }

  def postWithBearerToken(path: String, params: Map[String, String], token: String) = {
    val postRequest = new HttpPost(targetUrl(path))

    postRequest.setEntity(
      new UrlEncodedFormEntity(params.map {
        case (k, v) => new BasicNameValuePair(k, v)
      }.toList, "UTF-8")
    )

    execute(setAuthorizationHeader(postRequest, token))
  }

  def postPlainJson(path: String, json: String) = execute(createPlainJsonPostRequest(path, json))

  def postPlainJsonWithBearerToken(path: String, json: String, token: String) =
    execute(setAuthorizationHeader(createPlainJsonPostRequest(path, json), token))

  private[http] def createPlainJsonPostRequest(path: String, json: String) = {
    val postRequest = new HttpPost(targetUrl(path))

    val jsonEntity = new StringEntity(json)
    jsonEntity.setContentType("application/json")

    postRequest.setEntity(jsonEntity)
    postRequest.setHeader("Content-Type", "application/json")
    postRequest
  }

  private[http] def targetUrl(path: String): String

  private[http] def targetUrl(path: String, paramsAsString: String): String = {
    if (paramsAsString.nonEmpty) targetUrl(path + "?" + paramsAsString)
    else targetUrl(path)
  }

  private[http] def setAuthorizationHeader(request: HttpRequestBase, token: String) = {
    request.addHeader("Authorization", "Bearer " + token)
    request
  }

  private[http] def startupConnectionCleanerThread() {
    logger.info("Starting dead connection cleaner")

    cleanupThread.scheduleAtFixedRate(new Runnable {
      override def run = {
        logger.trace("Cleaning dead connections")
        connectionManager.closeExpiredConnections
        connectionManager.closeIdleConnections(10, TimeUnit.SECONDS)
      }
    }, 10, 10, TimeUnit.SECONDS)
  }

  private[http] def paramsToUrlParams(params: Map[String, Any]) = params.filterNot(_._2 == None).map {
    case (n, v) => v match {
      case Some(value) => addParam(n, value.toString)
      case _ => addParam(n, v.toString)
    }

  }.mkString("&")

  private[http] def addParam(name: String, value: String) = urlEncode(name) + "=" + urlEncode(value)

  private[http] def execute(request: HttpUriRequest) = {
    executeEither(request) match {
      case Left(result) => result
      case Right(exception) => throw exception
    }
  }

  private[http] def executeOptional(request: HttpUriRequest) = {
    executeEither(request) match {
      case Left(result) => Some(result)
      case Right(exception) => None
    }
  }

  private[http] def executeEither(request: HttpUriRequest): Either[String, ApiResponseException] = {
    logger.info("About to query: " + request.getMethod + " " + request.getURI)

    val response = httpClient.execute(request)

    val statusCode = response.getStatusLine.getStatusCode

    logger.info(request.getMethod + " " + request.getURI + " => " + statusCode)

    if (statusCode >= 400)
      Right(new ApiResponseException(statusCode = statusCode, message = EntityUtils.toString(response.getEntity, "UTF-8")))
    else
      Left(EntityUtils.toString(response.getEntity, "UTF-8"))
  }

  private[http] def jsonToPostOverWire(json: String) = new UrlEncodedFormEntity(List(new BasicNameValuePair("json", json)), "UTF-8")

  private[http] def jsonToPostOverWire(jsonParams: Map[String, String]) =
    new UrlEncodedFormEntity(jsonParams.toList.map(nameAndValue => new BasicNameValuePair(nameAndValue._1, nameAndValue._2)), "UTF-8")

  private[http] def configureHttpClient() = {
    val httpClient = new DefaultHttpClient(connectionManager)
    val httpParams = new BasicHttpParams()

    HttpConnectionParams.setConnectionTimeout(httpParams, Config("http.connectionTimeout", 10000))
    HttpConnectionParams.setSoTimeout(httpParams, Config("http.soTimeout", 10000))
    HttpClientParams.setRedirecting(httpParams, false)

    httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory))
    schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory))
    connectionManager.setMaxTotal(300)
    connectionManager.setDefaultMaxPerRoute(100)

    httpClient.setParams(httpParams)
    httpClient
  }
}

case class ApiResponseException(statusCode: Int, message: String) extends Exception(message)
