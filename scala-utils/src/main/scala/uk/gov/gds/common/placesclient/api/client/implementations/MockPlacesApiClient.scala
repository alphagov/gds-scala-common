package uk.gov.gds.placesclient.api.client.implementations

import uk.gov.gds.placesclient.model.{Authority, LocalAuthority, Address}
import uk.gov.gds.placesclient.api.client.PlacesApiClient
import uk.gov.gds.common.http.ApiResponseException

object MockPlacesApiClient extends PlacesApiClient {

  private case class MockAddressItem(postcode: String, addresses: List[Address])

  val camdenBoroughCouncil = LocalAuthority(name = "Camden Borough Council", opcsId = "00AG", gss = "E09000007")
  val wandsworthBoroughCouncil = LocalAuthority(name = "Wandsworth Borough Council", opcsId = "00BJ", gss = "E09000032")

  private def isCamdenCouncil(postcode: String) = postcode.replace(" ", "").equalsIgnoreCase("WC2B6SE")

  private def isWandsworthBoroughCouncil(postcode: String) = postcode.replace(" ", "").equalsIgnoreCase("SW112DR")

  def getLocalAuthority(address: Address) =
    if (isCamdenCouncil(address.postcode)) Some(camdenBoroughCouncil)
    else if (isWandsworthBoroughCouncil(address.postcode)) Some(wandsworthBoroughCouncil)
    else None

  def getLocalAuthority(postcode: String) =
    if (isCamdenCouncil(postcode)) Some(camdenBoroughCouncil)
    else if (isWandsworthBoroughCouncil(postcode)) Some(wandsworthBoroughCouncil)
    else None

  def getLocalAuthorityBySnac(snac: String) =
    if (snac == "00AG") Some(camdenBoroughCouncil)
    else if (snac == "00BJ") Some(wandsworthBoroughCouncil)
    else throw new ApiResponseException(404, "local authority details not found")

  def addressExists(postcode: String, lineOne: Option[String]) = getAddresses(postcode, lineOne).nonEmpty

  def numberAddressesFound(postcode: String, lineOne: Option[String]) = getAddresses(postcode, lineOne).size

  def getAuthorityByUrlSlug(urlSlug: String) = { throw new Exception("Not Implemented") }

  def getAuthorityLicenceInformationByAuthorityAndLicence(authorityUrlSlug: String, licenceUrlSlug: String) = { throw new Exception("Not Implemented") }

  def getAuthorityBySnacCode(snacCode: String) = { throw new Exception("Not Implemented") }

  def getAuthorityLicenceInformationBySnacCodeAndLegalRefNbr(snacCode: String, legalRefNbr: Int) = { throw new Exception("Not Implemented") }

  def getLicenceInformationByUrlSlugAndLegalRefNbr(urlSlug: String, legalReferenceNumber: Int) = { throw new Exception("Not Implemented") }

  def getLicenceInformationByLegalReferenceNumber(legalReferenceNumber: Int) = { throw new Exception("Not Implemented") }

  def getAllAuthorities() = { throw new Exception("Not Implemented") }

  def getAuthorityLicenceInteractions(authorityUrlSlug: String) = { throw new Exception("Not Implemented") }

  def getAllLicences() = { throw new Exception("Not Implemented") }

  def getCompetentAuthorityByPostcodeAndLicenceUrlSlug(postcode: String, licenceUrlSlug: String) = { throw new Exception("Not Implemented") }

  def getAddresses(postcode: String, lineOne: Option[String]): List[Address] = {
    mockAddressList.foreach {
      mockAddress =>

        if (mockAddress.postcode.replace(" ", "").equalsIgnoreCase(postcode.replace(" ", ""))) {
          return mockAddress.addresses.filter {
            address =>
              lineOne.isEmpty || lineOne.get.equalsIgnoreCase("") || address.lineOne.replace(" ", "").equalsIgnoreCase(lineOne.get.replace(" ", ""))
          }
        }
    }

    Nil
  }

  private lazy val mockAddressList = List(
    MockAddressItem(
      postcode = "WC2B 6SE",
      addresses = List(
        Address(
          lineOne = "Aviation House", lineTwo = "125 Kingsway", city = "London", postcode = "WC2B 6SE", county = "Camden", uprn = Some("26254659")
        )
      )),
    MockAddressItem(
      postcode = "SW11 2DR",
      addresses = List(
        Address(
          lineOne = "99 Latchmere Road", city = "London", postcode = "SW11 2DR", uprn = Some("23918643")
        ),
        Address(
          lineOne = "101 Latchmere Road", city = "London", postcode = "SW11 2DR", uprn = Some("23918638")
        ),
        Address(
          lineOne = "103 Latchmere Road", city = "London", postcode = "SW11 2DR", uprn = Some("23918639")
        )
      )
    )
  )
}
