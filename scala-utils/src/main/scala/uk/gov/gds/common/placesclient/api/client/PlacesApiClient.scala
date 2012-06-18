package uk.gov.gds.placesclient.api.client

import implementations.{RealPlacesApiClient, MockPlacesApiClient}
import uk.gov.gds.placesclient.model._

trait PlacesApiClient {
  def getAddresses(postcode: String, lineOne: Option[String]): List[Address]

  def addressExists(postcode: String, lineOne: Option[String]): Boolean

  def numberAddressesFound(postcode: String, lineOne: Option[String]): Int

  def getLocalAuthority(postcode: String): Option[LocalAuthority]

  def getLocalAuthority(address: Address): Option[LocalAuthority]

  def getLocalAuthorityBySnac(snac: String): Option[LocalAuthority]

  def getAuthorityByUrlSlug(urlSlug: String) : Option[Authority]

  def getAuthorityBySnacCode(snacCode: String) : Option[Authority]

  def getAuthorityLicenceInformationByAuthorityAndLicence(authorityUrlSlug: String, licenceUrlSlug: String) : Option[AuthorityLicenceInformation]

  def getAuthorityLicenceInformationBySnacCodeAndLegalRefNbr(snacCode: String, legalRefNbr: Int) : Option[AuthorityLicenceInformation]

  def getLicenceInformationByUrlSlugAndLegalRefNbr(urlSlug: String, legalReferenceNumber: Int) : Option[ElmsLicenceInformation]

  def getLicenceInformationByLegalReferenceNumber(legalReferenceNumber: Int) : Option[ElmsLicenceInformation]
}

object PlacesApiClient {

  private lazy val client = System.getProperty("MODE", "PROD_MODE") match {
    case "CACHED_MODE" => MockPlacesApiClient
    case "PROD_MODE" => RealPlacesApiClient
  }

  def getAddresses(postcode: String, lineOne: Option[String]) = client.getAddresses(postcode, lineOne)

  def addressExists(postcode: String, lineOne: Option[String]) = client.addressExists(postcode, lineOne)

  def numberAddressesFound(postcode: String, lineOne: Option[String]) = client.numberAddressesFound(postcode, lineOne)

  def getLocalAuthority(postcode: String): Option[LocalAuthority] = client.getLocalAuthority(postcode)

  def getLocalAuthority(address: Address) = client.getLocalAuthority(address)

  def getLocalAuthorityBySnac(snac: String) = client.getLocalAuthorityBySnac(snac)

  def getAuthorityByUrlSlug(urlSlug: String) = client.getAuthorityByUrlSlug(urlSlug)

  def getAuthorityBySnacCode(snacCode: String) = client.getAuthorityBySnacCode(snacCode)

  def getAuthorityLicenceInformationByAuthorityAndLicence(authorityUrlSlug: String, licenceUrlSlug: String) =
    client.getAuthorityLicenceInformationByAuthorityAndLicence(authorityUrlSlug, licenceUrlSlug)

  def getAuthorityLicenceInformationBySnacCodeAndLegalRefNbr(snacCode: String, legalRefNbr: Int) =
    client.getAuthorityLicenceInformationBySnacCodeAndLegalRefNbr(snacCode, legalRefNbr)

  def getLicenceInformationByUrlSlugAndLegalRefNbr(urlSlug: String, legalReferenceNumber: Int) = client.getLicenceInformationByUrlSlugAndLegalRefNbr(urlSlug, legalReferenceNumber)

  def getLicenceInformationByLegalReferenceNumber(legalReferenceNumber: Int) =
    client.getLicenceInformationByLegalReferenceNumber(legalReferenceNumber)

}

case class ApiResponseException(statusCode: Int, message: String) extends Exception(message)
