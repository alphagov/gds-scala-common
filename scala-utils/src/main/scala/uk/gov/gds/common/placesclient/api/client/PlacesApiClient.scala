package uk.gov.gds.placesclient.api.client

import implementations.{RealPlacesApiClient, MockPlacesApiClient}
import uk.gov.gds.placesclient.model.{Authority, LocalAuthority, Address}
import uk.gov.gds.common.logging.Logging

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

  def getAuthorityLicenceInformationBySnacCodeAndLegalRefNbr(sncCode: String, legalRefNbr: String) : Option[AuthorityLicenceInformation]

  def getLicenceInformationByUrlSlug(urlSlug: String) : Option[ElmsLicenceInformation]

  def getLicenceInformationByLegalReferenceNumber(legalReferenceNumber: Int) : Option[ElmsLicenceInformation]
}

object PlacesApiClient extends Logging{

  private lazy val client = {
    logger.info("Initialising PlacesApiClient MODE= "+System.getProperty("MODE", "PROD_MODE") +
                " Should be one of (CACHED_MODE or PROD_MODE)")
    System.getProperty("MODE", "PROD_MODE") match {
      case "CACHED_MODE" => MockPlacesApiClient
      case "PROD_MODE" => RealPlacesApiClient
      case _ => RealPlacesApiClient
    }
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

  def getAuthorityLicenceInformationBySnacCodeAndLegalRefNbr(sncCode: String, legalRefNbr: String) =
    client.getAuthorityLicenceInformationBySnacCodeAndLegalRefNbr(sncCode, legalRefNbr)

  def getLicenceInformationByUrlSlug(urlSlug: String) = client.getLicenceInformationByUrlSlug(urlSlug)

  def getLicenceInformationByLegalReferenceNumber(legalReferenceNumber: Int) =
    client.getLicenceInformationByLegalReferenceNumber(legalReferenceNumber)

}

case class ApiResponseException(statusCode: Int, message: String) extends Exception(message)
