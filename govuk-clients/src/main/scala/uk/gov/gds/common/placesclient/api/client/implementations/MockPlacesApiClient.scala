package uk.gov.gds.common.placesclient.api.client.implementations

import uk.gov.gds.common.model.{ LocalAuthority, Address }
import uk.gov.gds.common.http.ApiResponseException
import uk.gov.gds.common.placesclient.api.client.PlacesApiClient

object MockPlacesApiClient extends PlacesApiClient {

  private case class MockAddressItem(postcode: String, addresses: List[Address])

  val camdenBoroughCouncil = LocalAuthority(name = "Camden Borough Council", opcsId = "00AG", gssId = "E09000007")
  val wandsworthBoroughCouncil = LocalAuthority(name = "Wandsworth Borough Council", opcsId = "00BJ", gssId = "E09000032")
  val buryBoroughCouncil = LocalAuthority(name = "bury", opcsId = "00BM", gssId = "E08000002")
  val belfastCityCouncil = LocalAuthority("Belfast City Council", opcsId = "95Z", gssId = "95Z")
  val liverpoolCityCouncil = LocalAuthority("Liverpool City Council", opcsId = "00BY", gssId = "E08000012")

  val codeToAuthority = Map(
    camdenBoroughCouncil.opcsId -> camdenBoroughCouncil,
    wandsworthBoroughCouncil.opcsId -> wandsworthBoroughCouncil,
    buryBoroughCouncil.opcsId -> buryBoroughCouncil,
    belfastCityCouncil.opcsId -> belfastCityCouncil,
    liverpoolCityCouncil.opcsId -> liverpoolCityCouncil
  )

  val postCodesToAuthorities = Map(
    "WC2B6SE" -> camdenBoroughCouncil,
    "SW112DR" -> wandsworthBoroughCouncil,
    "M264LJ" -> buryBoroughCouncil,
    "BT71NT" -> belfastCityCouncil,
    "L40TH" -> liverpoolCityCouncil
  )

  def getLocalAuthority(address: Address) = postCodesToAuthorities.get(address.postcode.replace(" ", "").toUpperCase)

  def getLocalAuthority(postcode: String) = postCodesToAuthorities.get(postcode.replace(" ", "").toUpperCase)

  def getLocalAuthorityBySnac(snac: String) =
    codeToAuthority.get(snac).map(Some(_)).getOrElse(throw new ApiResponseException(404, "local authority details not found"))

  def addressExists(postcode: String, lineOne: Option[String]) = getAddresses(postcode, lineOne).nonEmpty

  def numberAddressesFound(postcode: String, lineOne: Option[String]) = getAddresses(postcode, lineOne).size

  def getAuthorityBySnacCode(snacCode: String) =
    throw new Exception("Not Implemented")

  //  def getAuthorityByUrlSlug(urlSlug: String) =
  //    throw new Exception("Not Implemented")
  //
  //  def getAuthorityLicenceInformationByAuthorityAndLicence(authorityUrlSlugWithArea: String, licenceUrlSlug: String) =
  //    throw new Exception("Not Implemented")
  //
  //  def getAllAuthorities() =
  //    throw new Exception("Not Implemented")

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
      )
    ),
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

  //  def getInteractionDetails(authorityUrlSlug: String, serviceId: Service): Option[Map[String, String]] =
  //  Option(Map("licenceName"-> "Animal boarding establishment lic.",
  //  "lgslId"-> "374",
  //  "interactionName"-> "Application to renew an animal boarding establishment lic."
  //  ))
}
