package uk.gov.gds.common.model

case class Authority(
  name: String,
  agencyId: Int,
  urlSlug: String,
  authorityUrl: Option[String] = None,
  snacCode: Option[String] = None,
  level: String,
  country: Option[String] = None,
  countries: Set[String] = Set.empty,
  snacCodes: Set[String] = Set.empty
)
    extends HasIdentity {

  def id = urlSlug

  def covers(local: Authority): Boolean =
    countries != Set.empty && {
      val co = countries
      val sn = snacCodes

      co.contains(local.country.get) && (sn.isEmpty || sn.contains(local.snacCode.get))
    }

  def getCountryForWorkingDays: String = {
    country match {
      case Some(aCountry) => aCountry
      case _ => countries.size match {
        case 1 => countries.toList(0)
        case _ => "England"
      }
    }
  }
}

object Authority {
  def urlSlugFromName(name: String) = name.replace(" ", "-").replace(".", "").toLowerCase
}

case class LocalAuthority(
  name: String = "",
    ero: Ero = Ero(),
    opcsId: String,
    gssId: String = ""
) extends HasIdentity {
  def id = opcsId
}

object LocalAuthority {
  def apply(a: Authority) = new LocalAuthority(name = a.name, ero = Ero(), opcsId = a.snacCode.get, gssId = "")
}

case class Ero(address: Option[GovUkAddress] = None, telephoneNumber: String = "")

case class GovUkAddress(
  lineOne: String = "",
    lineTwo: String = "",
    lineThree: String = "",
    lineFour: String = "",
    lineFive: String = "",
    city: String = "",
    postcode: String = "",
    county: String = "",
    uprn: Option[String] = None
) {

  def shortString = if (lineOne.length() > 0)
    lineOne + " " + postcode
  else
    ""

  def asString = List(lineOne, lineTwo, lineThree, lineFour, lineFive, city, postcode)
    .filterNot(_.isEmpty).mkString(", ")

  def asShortString = List(lineOne, lineTwo, lineThree, lineFour, lineFive)
    .filterNot(_.isEmpty).reduceLeft(_ + ", " + _)
}