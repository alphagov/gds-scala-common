package uk.gov.gds.common.placesclient.api.client.implementations

import uk.gov.gds.common.json.JsonSerializer._
import uk.gov.gds.common.placesclient.api.client.PlacesHttpClient
import uk.gov.gds.common.placesclient.model._
import uk.gov.gds.common.placesclient.api.client.PlacesApiClient

object RealPlacesApiClient extends PlacesApiClient {

  def getAddresses(postcode: String, lineOne: Option[String]) = {
    val params = Map("postcode" -> postcode) ++ (lineOne match {
      case Some(thing) => Map("lineOne" -> thing)
      case _ => Map.empty
    })

    val response = PlacesHttpClient.get("/address", params)
    fromJson[List[Address]](response)
  }

  def addressExists(postcode: String, lineOne: Option[String]) = getAddresses(postcode, lineOne).nonEmpty

  def numberAddressesFound(postcode: String, lineOne: Option[String]) = getAddresses(postcode, lineOne).size

  def getLocalAuthority(postcode: String): Option[LocalAuthority] =
    PlacesHttpClient.getOptional("/authority", Map("postcode" -> postcode)).flatMap(fromJson[Option[LocalAuthority]](_))

  def getLocalAuthority(address: Address) =
    PlacesHttpClient.getOptional("/authority", Map("postcode" -> address.postcode)).flatMap(fromJson[Option[LocalAuthority]](_))

  def getLocalAuthorityBySnac(snac: String) =
    PlacesHttpClient.getOptional("/authority/ertp/" + snac).flatMap(fromJson[Option[LocalAuthority]](_))
}

