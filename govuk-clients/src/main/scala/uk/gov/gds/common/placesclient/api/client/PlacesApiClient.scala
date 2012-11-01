package uk.gov.gds.common.placesclient.api.client

import uk.gov.gds.common.placesclient.api.client.implementations.{RealPlacesApiClient, MockPlacesApiClient}
import uk.gov.gds.common.logging.Logging
import uk.gov.gds.common.model.LocalAuthority
import uk.gov.gds.common.model.Address

trait PlacesApiClient {
  def getAddresses(postcode: String, lineOne: Option[String]): List[Address]

  def addressExists(postcode: String, lineOne: Option[String]): Boolean

  def numberAddressesFound(postcode: String, lineOne: Option[String]): Int

  def getLocalAuthority(postcode: String): Option[LocalAuthority]

  def getLocalAuthority(address: Address): Option[LocalAuthority]

  def getLocalAuthorityBySnac(snac: String): Option[LocalAuthority]
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
}

case class ApiResponseException(statusCode: Int, message: String) extends Exception(message)
