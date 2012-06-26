package uk.gov.gds.placesclient.simplemodel

import uk.gov.gds.placesclient.model.Authority

case class SimpleAuthority(name: String,
                     agencyId: Int,
                     urlSlug: String,
                     snacCode: Option[String] = None,
                     level: String,
                     country: Option[String] = None) {
  def this(a: Authority) = this(a.name, a.agencyId, a.urlSlug, a.snacCode, a.level, a.country)
}

case class SimpleLicences(licences: Map[Int, SimpleAuthority])