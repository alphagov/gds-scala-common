package uk.gov.gds.placesclient.api.client.implementations

import uk.gov.gds.common.json.JsonSerializer._
import org.apache.http.util.EntityUtils
import uk.gov.gds.placesclient.api.client.{PlacesApiClient, PlacesHttpClient}
import uk.gov.gds.placesclient.model._

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
    PlacesHttpClient.getOptional("/authority", Map("postcode" -> postcode)).flatMap(fromJson[Option[LocalAuthority]](_))
  }

  def getLocalAuthority(address: Address) = {
    PlacesHttpClient.getOptional("/authority", Map("postcode" -> address.postcode)).flatMap(fromJson[Option[LocalAuthority]](_))
  }

  def getLocalAuthorityBySnac(snac: String) = {
    PlacesHttpClient.getOptional("/authority/ertp/" + snac).flatMap(fromJson[Option[LocalAuthority]](_))
  }

  def getAuthorityByUrlSlug(urlSlug: String) = {
    PlacesHttpClient.getOptional("/authority/"+urlSlug).flatMap(fromJson[Option[Authority]](_))
  }

  def getAuthorityBySnacCode(snacCode: String) = {
    PlacesHttpClient.getOptional("/authority/"+snacCode).flatMap(fromJson[Option[Authority]](_))
  }

  def getAuthorityLicenceInformationByAuthorityAndLicence(authorityUrlSlug: String, licenceUrlSlug: String) = {
    PlacesHttpClient.getOptional("/elms-licence/"+authorityUrlSlug+"/"+licenceUrlSlug).flatMap(fromJson[Option[AuthorityLicenceInformation]](_))
  }

  def getAuthorityLicenceInformationBySnacCodeAndLegalRefNbr(snacCode: String, legalRefNbr: Int) = {
    PlacesHttpClient.getOptional("/elms-licence/"+legalRefNbr+"/"+snacCode).flatMap(fromJson[Option[AuthorityLicenceInformation]](_))
  }

  def getLicenceInformationByUrlSlugAndLegalRefNbr(urlSlug: String, legalReferenceNumber: Int) = {
    PlacesHttpClient.getOptional("/elms-licence/"+urlSlug+"/"+legalReferenceNumber).flatMap(fromJson[Option[ElmsLicenceInformation]](_))
  }

  def getLicenceInformationByLegalReferenceNumber(legalReferenceNumber: Int) = {
    PlacesHttpClient.getOptional("/elms-licence/"+legalReferenceNumber).flatMap(fromJson[Option[ElmsLicenceInformation]](_))
  }
}

