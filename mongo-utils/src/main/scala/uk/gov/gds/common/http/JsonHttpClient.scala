package uk.gov.gds.common.http

import org.apache.http.client.methods.{ HttpGet, HttpPost }
import org.apache.http.entity.StringEntity
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair
import scala.collection.JavaConversions._
import uk.gov.gds.common.json.MongoJsonSerializer._

abstract class JsonHttpClient extends ApacheHttpClient {

  override def getFromJson[A](url: String, params: String*)(implicit m: Manifest[A]) =
    fromJson[A](get(String.format(url, params.map(urlEncode(_)): _*)))

  override def getWithJson(url: String, params: String*) = get(String.format(url, params.map(urlEncode(_)): _*))

  override def postJson(path: String, json: String) = {
    val postRequest = new HttpPost(targetUrl(path))
    postRequest.setEntity(jsonToPostOverWire(json))

    execute(postRequest)
  }

  override def postJson(path: String, jsonParams: Map[String, String]) = {
    val postRequest = new HttpPost(targetUrl(path))

    postRequest.setEntity(jsonToPostOverWire(jsonParams))
    execute(postRequest)
  }

  override def postWithBearerToken(path: String, params: Map[String, String], token: String) = {
    val postRequest = new HttpPost(targetUrl(path))

    postRequest.setEntity(
      new UrlEncodedFormEntity(params.map {
        case (k, v) => new BasicNameValuePair(k, v)
      }.toList, "UTF-8")
    )

    execute(setAuthorizationHeader(postRequest, token))
  }

  override def postPlainJson(path: String, json: String) = execute(createPlainJsonPostRequest(path, json))

  override def postPlainJsonWithBearerToken(path: String, json: String, token: String) =
    execute(setAuthorizationHeader(createPlainJsonPostRequest(path, json), token))

  override private[http] def createPlainJsonPostRequest(path: String, json: String) = {
    val postRequest = new HttpPost(targetUrl(path))

    val jsonEntity = new StringEntity(json)
    jsonEntity.setContentType("application/json")

    postRequest.setEntity(jsonEntity)
    postRequest.setHeader("Content-Type", "application/json")
    postRequest
  }

  override private[http] def jsonToPostOverWire(json: String) = new UrlEncodedFormEntity(List(new BasicNameValuePair("json", json)), "UTF-8")

  override private[http] def jsonToPostOverWire(jsonParams: Map[String, String]) =
    new UrlEncodedFormEntity(jsonParams.toList.map(nameAndValue => new BasicNameValuePair(nameAndValue._1, nameAndValue._2)), "UTF-8")

}