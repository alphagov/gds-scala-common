package uk.gov.gds.placesclient.api.client.implementations

import uk.gov.gds.common.json.JsonSerializer._
import org.apache.http.util.EntityUtils
import uk.gov.gds.placesclient.model.{LocalAuthority, Address}
import uk.gov.gds.placesclient.api.client.{PlacesApiClient, PlacesHttpClient}

object RealPlacesApiClient extends PlacesApiClient {

  def getAddresses(postcode: String, lineOne: Option[String]) = {
    val params = Map("postcode" -> postcode) ++ (lineOne match {
      case Some(thing) => Map("lineOne" -> thing)
      case _ => Map.empty
    })

    val response = PlacesHttpClient.getWithResponse("/address", params)

    if (response.getStatusLine.getStatusCode == 404)
      List.empty
    else
      fromJson[List[Address]](EntityUtils.toString(response.getEntity, "UTF-8"))
  }

  def addressExists(postcode: String, lineOne: Option[String]) = getAddresses(postcode, lineOne).nonEmpty

  def numberAddressesFound(postcode: String, lineOne: Option[String]) = getAddresses(postcode, lineOne).size

  def getLocalAuthority(postcode: String): Option[LocalAuthority] = {
    val response = PlacesHttpClient.getWithResponse("/local-authority", Map("postcode" -> postcode))
    fromJson[Option[LocalAuthority]](EntityUtils.toString(response.getEntity, "UTF-8"))
  }

  def getLocalAuthority(address: Address) = {
    val response = PlacesHttpClient.getWithResponse("/local-authority", Map("postcode" -> address.postcode))
    fromJson[Option[LocalAuthority]](EntityUtils.toString(response.getEntity, "UTF-8"))
  }

  def getLocalAuthorityBySnac(snac: String) = {
    val response = PlacesHttpClient.getWithResponse("/local-authority/" + snac)
    fromJson[Option[LocalAuthority]](EntityUtils.toString(response.getEntity, "UTF-8"))
  }
}

