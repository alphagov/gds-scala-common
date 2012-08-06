package uk.gov.gds.common.localauth

import uk.gov.gds.common.repository.HasIdentity

case class Authority(name: String,
                     agencyId: Int,
                     urlSlug: String,
                     authorityUrl: Option[String] = None,
                     snacCode: Option[String] = None,
                     level: String,
                     country: Option[String] = None,
                     countries: Set[String] = Set.empty,
                     snacCodes: Set[String] = Set.empty)
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
