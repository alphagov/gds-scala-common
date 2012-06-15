package uk.gov.gds.placesclient.api.client.implementations

import uk.gov.gds.common.json.JsonSerializer._
import org.apache.http.util.EntityUtils
import uk.gov.gds.placesclient.api.client.{PlacesApiClient, PlacesHttpClient}
import uk.gov.gds.placesclient.model.{AuthorityLicenceInteraction, Authority, LocalAuthority, Address}

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

  def getLocalAuthority(postcode: String): Option[LocalAuthority] = {
    val response = PlacesHttpClient.get("/authority", Map("postcode" -> postcode))
    fromJson[Option[LocalAuthority]](response)
  }

  def getLocalAuthority(address: Address) = {
    val response = PlacesHttpClient.get("/authority", Map("postcode" -> address.postcode))
    fromJson[Option[LocalAuthority]](response)
  }

  def getLocalAuthorityBySnac(snac: String) = {
    val response = PlacesHttpClient.get("/authority/ertp/" + snac)
    fromJson[Option[LocalAuthority]](response)
  }

  def getAuthorityByUrlSlug(urlSlug: String) = {
    val response = PlacesHttpClient.get("/authority/"+urlSlug)
    fromJson[Option[Authority]](response)
  }

  def getAuthorityLicenceInteractionsByAuthorityAndLicence(authorityUrlSlug: String, licenceUrlSlug: String) = {
    val response = PlacesHttpClient.get("/elms-licence/"+authorityUrlSlug+"/"+licenceUrlSlug)
    fromJson[Option[AuthorityLicenceInteraction]](response)
  }
}

