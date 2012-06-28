package uk.gov.gds.common.placesclient.api.client

import uk.gov.gds.common.placesclient.api.client.implementations.{RealPlacesApiClient, MockPlacesApiClient}
import uk.gov.gds.common.logging.Logging
import uk.gov.gds.placesclient.model._

trait PlacesApiClient {
  def getAddresses(postcode: String, lineOne: Option[String]): List[Address]

  def addressExists(postcode: String, lineOne: Option[String]): Boolean

  def numberAddressesFound(postcode: String, lineOne: Option[String]): Int

  def getLocalAuthority(postcode: String): Option[LocalAuthority]

  def getLocalAuthority(address: Address): Option[LocalAuthority]

  def getLocalAuthorityBySnac(snac: String): Option[LocalAuthority]

  def getAuthorityByUrlSlug(urlSlug: String): Option[Authority]

  def getAuthorityBySnacCode(snacCode: String): Option[Authority]

  def getAuthorityLicenceInformationByAuthorityAndLicence(authorityUrlSlugWithArea: String, licenceUrlSlug: String): Option[AuthorityLicenceInformation]

  def getAuthorityLicenceInformationBySnacCodeAndLegalRefNbr(snacCode: String, legalRefNbr: Int): Option[AuthorityLicenceInformation]

  def getLicenceInformationByUrlSlugAndLegalRefNbr(urlSlug: String, legalReferenceNumber: Int): Option[ElmsLicenceInformation]

  def getLicenceInformationByLegalReferenceNumber(legalReferenceNumber: Int): Option[ElmsLicenceInformation]

  def getAllAuthorities(): Option[List[Authority]]

  def getAuthorityLicenceInteractions(authorityUrlSlug: String): Option[List[AuthorityLicenceInteraction]]

  def getAllLicences(): Option[List[ElmsLicence]]

  def getCompetentAuthoritiesByPostcodeAndLicenceUrlSlug(postcode: String, licenceUrlSlug: String): Option[List[AuthorityLicenceInformation]]

  def getLicenceInteractionsByPdfName(pdfName: String): Option[List[LicenceInteraction]]
}

object PlacesApiClient extends Logging {

  private lazy val client = {
    logger.info("Initialising PlacesApiClient MODE= " + System.getProperty("MODE", "PROD_MODE") +
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

  def getAuthorityLicenceInformationBySnacCodeAndLegalRefNbr(snacCode: String, legalRefNbr: Int) =
    client.getAuthorityLicenceInformationBySnacCodeAndLegalRefNbr(snacCode, legalRefNbr)

  def getLicenceInformationByUrlSlugAndLegalRefNbr(urlSlug: String, legalReferenceNumber: Int) = client.getLicenceInformationByUrlSlugAndLegalRefNbr(urlSlug, legalReferenceNumber)

  def getLicenceInformationByLegalReferenceNumber(legalReferenceNumber: Int) =
    client.getLicenceInformationByLegalReferenceNumber(legalReferenceNumber)

  def getAllAuthorities() = client.getAllAuthorities()

  def getAuthorityLicenceInteractions(authorityUrlSlug: String) = client.getAuthorityLicenceInteractions(authorityUrlSlug)

  def getAllLicences() = client.getAllLicences()

  def getCompetentAuthoritiesByPostcodeAndLicenceUrlSlug(postcode: String, licenceUrlSlug: String) = client.getCompetentAuthoritiesByPostcodeAndLicenceUrlSlug(postcode, licenceUrlSlug)

  def getLicenceInteractionsByPdfName(pdfName: String) = client.getLicenceInteractionsByPdfName(pdfName)
}

case class ApiResponseException(statusCode: Int, message: String) extends Exception(message)
