package uk.gov.gds.placesclient.api.client

import implementations.{RealPlacesApiClient, MockPlacesApiClient}
import uk.gov.gds.placesclient.model.{LocalAuthority, Address}

trait PlacesApiClient {
  def getAddresses(postcode: String, lineOne: Option[String]): List[Address]

  def addressExists(postcode: String, lineOne: Option[String]): Boolean

  def numberAddressesFound(postcode: String, lineOne: Option[String]): Int

  def getLocalAuthority(postcode: String): Option[LocalAuthority]

  def getLocalAuthority(address: Address): Option[LocalAuthority]

  def getLocalAuthorityBySnac(snac: String): Option[LocalAuthority]
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
}

case class ApiResponseException(statusCode: Int, message: String) extends Exception(message)
