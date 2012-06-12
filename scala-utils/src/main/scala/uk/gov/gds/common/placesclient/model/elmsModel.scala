package uk.gov.gds.placesclient.model

import uk.gov.gds.common.repository.HasIdentity

case class ElmsAdminArea(code: String, name: String) extends HasIdentity {
  def id = code
}

case class ElmsLicence(name: String,
                       legalReferenceNumber: String,
                       legislationName: String,
                       urlSlug: String,
                       lgslId: Int,
                       elmsAdminArea: ElmsAdminArea) extends HasIdentity {

  def id = legalReferenceNumber

}

case class ElmsLicenceInformation(elmsLicence : ElmsLicence,
                                   allUkAuthority : Option[Authority] = None)

case class Licences(licences: java.util.Map[Int, Authority])

case class Authority(name: String,
                     agencyId: Int,
                     urlSlug: String,
                     snacCode: Option[String] = None,
                     level: String,
                     country: Option[String] = None,
                     gss: Option[String] = None,
                     countries: Set[String] = null,
                     snacCodes: Set[String] = null)
extends HasIdentity {
  def id = urlSlug

  def covers(local: Authority): Boolean =
    countries != null &
    countries.contains(local.country.get) &
    (snacCodes.isEmpty | snacCodes.contains(local.snacCode.get))
}

object Authority {
  def urlSlugFromName(name: String) = name.replace(" ","-").replace(".","").toLowerCase
}

case class LicenceInteraction(lgilId: Int,
                              lgilSubId: Int,
                              licenceInteractionName: String)

case class AuthorityLicenceInteraction(agencyId: Int,
                                       licenceId: Int, mode: Int,
                                       licenceInteraction: LicenceInteraction) extends HasIdentity {
  def id = agencyId+"/"+licenceId+"/"+licenceInteraction.lgilId+"/"+licenceInteraction.lgilSubId
}

case class AuthorityLicenceInformation(authority: Authority, licence: ElmsLicence,
                                       authorityLicenceInteractions: Map[Int, Set[AuthorityLicenceInteraction]]) {
}