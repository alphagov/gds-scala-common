package uk.gov.gds.common.http

import uk.gov.gds.common.j2ee.ContainerEventListener
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.util.EntityUtils
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpConnectionParams
import org.apache.http.client.params.HttpClientParams
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.ssl.SSLSocketFactory
import uk.gov.gds.common.logging.Logging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import scala.collection.JavaConversions._
import uk.gov.gds.common.json.JsonSerializer._

abstract class ApacheHttpClient extends ContainerEventListener with UrlEncoding with Logging {

  private lazy val schemeRegistry = new SchemeRegistry
  private lazy val connectionManager = new ThreadSafeClientConnManager(schemeRegistry)
  private lazy val httpClient = configureHttpClient()
  private lazy val cleanupThread = Executors.newScheduledThreadPool(1)

  protected def targetUrl(path: String): String

  protected def targetUrl(path: String, paramsAsString: String): String = {
    if (paramsAsString.nonEmpty) targetUrl(path + "?" + paramsAsString)
    else targetUrl(path)
  }

  def get(path: String, params: Map[String, Any] = Map.empty) = {
    execute(new HttpGet(targetUrl(path, paramsToUrlParams(params))))
  }

  def getWithResponse(path: String, params: Map[String, Any] = Map.empty) = {
    executeWithResponse(new HttpGet(targetUrl(path, paramsToUrlParams(params))))
  }

  def post(path: String) = execute(new HttpPost(targetUrl(path)))

  def post(path: String, params: Map[String, String]) = {
    val postRequest = new HttpPost(targetUrl(path))

    postRequest.setEntity(
      new UrlEncodedFormEntity(params.map {case (k,v) => new BasicNameValuePair(k,v)}.toList, "UTF-8")
    )
    execute(postRequest)
  }

  def getFromJson[A](url: String, params: String*)(implicit m: Manifest[A]) =
    fromJson[A](get(String.format(url, params.map(urlEncode(_)): _*)))

  def getWithJson(url: String, params: String*) = get(String.format(url, params.map(urlEncode(_)): _*))

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

  override def startup() {
    logger.info("Starting dead connection cleaner")

    cleanupThread.scheduleAtFixedRate(new Runnable {
      override def run = {
        logger.trace("Cleaning dead connections")
        connectionManager.closeExpiredConnections
        connectionManager.closeIdleConnections(10, TimeUnit.SECONDS)
      }
    }, 10, 10, TimeUnit.SECONDS)
  }

  override def shutdown() {
    if (!cleanupThread.isShutdown) {
      logger.info("Closing down dead connection cleaner")
      cleanupThread.shutdown()
    }
  }

  private def paramsToUrlParams(params: Map[String, Any]) = params.map {
    case (n, v) =>
      v match {
        case None =>
        case Some(value) => addParam(n, value.toString)
        case _ => addParam(n, v.toString)
      }

  }.mkString("&")

  private def addParam(name: String, value: String) = urlEncode(name) + "=" + urlEncode(value)

  def postWithResponse(path: String) = executeWithResponse(new HttpPost(targetUrl(path)))

  private def execute(request: HttpUriRequest) = {
    logger.info("About to query: " + request.getMethod + " " + request.getURI)

    val response = httpClient.execute(request)
    val statusCode = response.getStatusLine.getStatusCode

    if (statusCode < 200 || statusCode >= 400) {
      throw new ApiResponseException(statusCode = statusCode, message = EntityUtils.toString(response.getEntity, "UTF-8"))
    }

    EntityUtils.toString(response.getEntity, "UTF-8")
  }

  private def executeWithResponse(request: HttpUriRequest) = {
    logger.trace("About to query: " + request.getMethod + " " + request.getURI)

    val response = httpClient.execute(request)
    val statusCode = response.getStatusLine.getStatusCode

    logger.info(request.getMethod + " " + request.getURI + " => " + statusCode)
    response
  }

  private def jsonToPostOverWire(json: String) = new UrlEncodedFormEntity(List(new BasicNameValuePair("json", json)), "UTF-8")

  private def jsonToPostOverWire(jsonParams: Map[String, String]) =
    new UrlEncodedFormEntity(jsonParams.toList.map(nameAndValue => new BasicNameValuePair(nameAndValue._1, nameAndValue._2)), "UTF-8")

  private def configureHttpClient() = {
    val httpClient = new DefaultHttpClient(connectionManager)
    val httpParams = new BasicHttpParams()

    HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
    HttpConnectionParams.setSoTimeout(httpParams, 10000);
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