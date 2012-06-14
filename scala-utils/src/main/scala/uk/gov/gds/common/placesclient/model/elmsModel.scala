package uk.gov.gds.placesclient.model

import uk.gov.gds.common.repository.HasIdentity
import com.mongodb.casbah.Imports.BasicDBList
import com.mongodb.casbah.commons.MongoDBObject

case class ElmsAdminArea(code: String, name: String) extends HasIdentity {
  def id = code
}

case class ElmsLicence(name: String,
                       legalReferenceNumber: String,
                       legislationName: String,
                       urlSlug: String,
                       licenceUrl: Option[String] = None,
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
                     authorityUrl: Option[String] = None,
                     snacCode: Option[String] = None,
                     level: String,
                     country: Option[String] = None,
                     gss: Option[String] = None,
                     countries: Option[Set[String]] = None,
                     snacCodes: Option[Set[String]] = None)
extends HasIdentity {
  def id = urlSlug

  def covers(local: Authority): Boolean = {
    val co = if (countries.get.isInstanceOf[BasicDBList])
      countries.get.asInstanceOf[com.mongodb.BasicDBList].toArray.toSet
    else countries.get
    val sn = if (snacCodes.get.isInstanceOf[BasicDBList])
      snacCodes.get.asInstanceOf[com.mongodb.BasicDBList].toArray.toSet
    else snacCodes.get ;
    co.contains(local.country.get) && (sn.isEmpty || sn.contains(local.snacCode.get))
  }
}

object Authority {
  def urlSlugFromName(name: String) = name.replace(" ","-").replace(".","").toLowerCase
}

case class LicenceInteraction(lgilId: Int,
                              interactionType: String,
                              lgilSubId: Int,
                              licenceInteractionName: String,
                              form: Option[LicenceForm] = None,
                              externalUrl: Option[String] = None)

object LicenceInteraction {
  def lgslIdToInteractionTypeMap = { Map[Int, String](0 -> "apply", 4 -> "pay for", 11 -> "change", 14 -> "renew",
    30 -> "dont know yet", 31 -> "dont know yet", 32 -> "dont know yet")
  }
}

case class AuthorityLicenceInteraction(agencyId: Int,
                                       licenceId: Int, mode: Int,
                                       licenceInteraction: LicenceInteraction) extends HasIdentity {
  def id = agencyId+"/"+licenceId+"/"+licenceInteraction.lgilId+"/"+licenceInteraction.lgilSubId
}

case class AuthorityLicenceInformation(authority: Authority, licence: ElmsLicence,
                                       authorityLicenceInteractions: Map[String, List[AuthorityLicenceInteraction]]) {
}

case class LicenceForm(name: String,
                       subForm: Int,
                       formRefNo: Long,
                       fileName: String,
                       fileUrl: Option[String] = None,
                       fileSizeInBytes: Long,
                       formVersion: Int) {

}